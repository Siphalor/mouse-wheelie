/*
 * Copyright 2021 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client.inventory.sort;

import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class InventorySorter {
	private final HandledScreen<?> containerScreen;
	private List<Slot> inventorySlots;
	private final ItemStack[] stacks;

	public InventorySorter(HandledScreen<?> containerScreen, Slot originSlot) {
		this.containerScreen = containerScreen;

		collectSlots(originSlot);

		this.stacks = inventorySlots.stream().map(slot -> slot.getStack().copy()).toArray(ItemStack[]::new);
	}

	private void collectSlots(Slot originSlot) {
		inventorySlots = new ArrayList<>();
		ContainerScreenHelper<? extends HandledScreen<?>> screenHelper = new ContainerScreenHelper<>(containerScreen, (slot, data, slotActionType) -> {
		});
		int originScope = screenHelper.getScope(originSlot);
		if (originScope == ContainerScreenHelper.INVALID_SCOPE) {
			return;
		}
		for (Slot slot : containerScreen.getScreenHandler().slots) {
			if (originScope == screenHelper.getScope(slot)) {
				inventorySlots.add(slot);
			}
		}
	}

	private void combineStacks() {
		ItemStack stack;
		ArrayDeque<InteractionManager.ClickEvent> clickEvents = new ArrayDeque<>();
		for (int i = stacks.length - 1; i >= 0; i--) {
			stack = stacks[i];
			if (stack.isEmpty()) continue;
			int stackSize = stack.getCount();
			if (stackSize >= stack.getItem().getMaxCount()) continue;
			clickEvents.add(new InteractionManager.ClickEvent(containerScreen.getScreenHandler().syncId, inventorySlots.get(i).id, 0, SlotActionType.PICKUP));
			for (int j = 0; j < i; j++) {
				ItemStack targetStack = stacks[j];
				if (targetStack.isEmpty()) continue;
				if (targetStack.getCount() >= targetStack.getItem().getMaxCount()) continue;
				if (stack.getItem() == targetStack.getItem() && ItemStack.areTagsEqual(stack, targetStack)) {
					int delta = targetStack.getItem().getMaxCount() - targetStack.getCount();
					delta = Math.min(delta, stackSize);
					stackSize -= delta;
					targetStack.setCount(targetStack.getCount() + delta);
					clickEvents.add(new InteractionManager.ClickEvent(containerScreen.getScreenHandler().syncId, inventorySlots.get(j).id, 0, SlotActionType.PICKUP));
					if (stackSize <= 0) break;
				}
			}
			if (clickEvents.size() <= 1) {
				clickEvents.clear();
				continue;
			}
			InteractionManager.interactionEventQueue.addAll(clickEvents);
			InteractionManager.triggerSend(InteractionManager.TriggerType.GUI_CONFIRM);
			clickEvents.clear();
			if (stackSize > 0) {
				InteractionManager.pushClickEvent(containerScreen.getScreenHandler().syncId, inventorySlots.get(i).id, 0, SlotActionType.PICKUP);
				stack.setCount(stackSize);
			} else {
				stacks[i] = ItemStack.EMPTY;
			}
		}
	}

	public void sort(SortMode sortMode) {
		combineStacks();
		ItemStack currentStack;
		final int slotCount = stacks.length;
		Integer[] sortIds = IntStream.range(0, slotCount).boxed().toArray(Integer[]::new);

		sortIds = sortMode.sort(sortIds, stacks);

		BitSet doneSlashEmpty = new BitSet(slotCount * 2);
		for (int i = 0; i < slotCount; i++) {
			if (i == sortIds[i]) {
				doneSlashEmpty.set(i);
				continue;
			}
			if (stacks[i].isEmpty()) doneSlashEmpty.set(slotCount + i);
		}
		for (int i = 0; i < slotCount; i++) {
			if (doneSlashEmpty.get(i)) {
				continue;
			}
			if (doneSlashEmpty.get(slotCount + sortIds[i])) {
				doneSlashEmpty.set(sortIds[i]);
				continue;
			}
			InteractionManager.pushClickEvent(containerScreen.getScreenHandler().syncId, inventorySlots.get(sortIds[i]).id, 0, SlotActionType.PICKUP);
			doneSlashEmpty.set(slotCount + sortIds[i]);
			currentStack = stacks[sortIds[i]];
			int id = i;
			do {
				if (
						stacks[id].getItem() == currentStack.getItem()
								&& stacks[id].getCount() == currentStack.getCount()
								&& !doneSlashEmpty.get(slotCount + id)
								&& ItemStack.areTagsEqual(stacks[id], currentStack)
				) {
					doneSlashEmpty.set(id);
					id = ArrayUtils.indexOf(sortIds, id);
					continue;
				}

				InteractionManager.pushClickEvent(containerScreen.getScreenHandler().syncId, inventorySlots.get(id).id, 0, SlotActionType.PICKUP);
				currentStack = stacks[id];
				doneSlashEmpty.set(id);
				if (doneSlashEmpty.get(slotCount + id)) {
					break;
				}
				id = ArrayUtils.indexOf(sortIds, id);
			} while (!doneSlashEmpty.get(id));
		}
	}
}
