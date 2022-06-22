/*
 * Copyright 2020-2022 Siphalor
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
import java.util.Arrays;
import java.util.BitSet;

@Environment(EnvType.CLIENT)
public class InventorySorter {
	private final ContainerScreenHelper<? extends HandledScreen<?>> screenHelper;
	private final HandledScreen<?> containerScreen;
	private Slot[] inventorySlots;
	private final ItemStack[] stacks;

	/**
	 * @deprecated Use {@link #InventorySorter(ContainerScreenHelper, HandledScreen, Slot)} instead
	 */
	@Deprecated
	public InventorySorter(HandledScreen<?> containerScreen, Slot originSlot) {
		this(
				ContainerScreenHelper.of(
						containerScreen,
						(slot, action, slotActionType) -> new InteractionManager.ClickEvent(
								containerScreen.getScreenHandler().syncId,
								slot.id, action, slotActionType
						)
				),
				containerScreen,
				originSlot
		);
	}

	public InventorySorter(ContainerScreenHelper<? extends HandledScreen<?>> screenHelper, HandledScreen<?> containerScreen, Slot originSlot) {
		this.screenHelper = screenHelper;
		this.containerScreen = containerScreen;

		collectSlots(originSlot);

		this.stacks = new ItemStack[inventorySlots.length];
		for (int i = 0; i < inventorySlots.length; i++) {
			stacks[i] = inventorySlots[i].getStack();
		}
	}

	private void collectSlots(Slot originSlot) {
		int originScope = screenHelper.getScope(originSlot);
		if (originScope == ContainerScreenHelper.INVALID_SCOPE) {
			this.inventorySlots = new Slot[0];
			return;
		}
		ArrayList<Slot> inventorySlots = new ArrayList<>();
		for (Slot slot : containerScreen.getScreenHandler().slots) {
			if (originScope == screenHelper.getScope(slot)) {
				inventorySlots.add(slot);
			}
		}
		this.inventorySlots = inventorySlots.toArray(new Slot[0]);
	}

	private void combineStacks() {
		ItemStack stack;
		ArrayDeque<InteractionManager.InteractionEvent> clickEvents = new ArrayDeque<>();
		for (int i = stacks.length - 1; i >= 0; i--) {
			stack = stacks[i];
			if (stack.isEmpty()) continue;
			int stackSize = stack.getCount();
			if (stackSize >= stack.getItem().getMaxCount()) continue;
			clickEvents.add(screenHelper.createClickEvent(inventorySlots[i], 0, SlotActionType.PICKUP));
			for (int j = 0; j < i; j++) {
				ItemStack targetStack = stacks[j];
				if (targetStack.isEmpty()) continue;
				if (targetStack.getCount() >= targetStack.getItem().getMaxCount()) continue;
				if (stack.getItem() == targetStack.getItem() && ItemStack.areTagsEqual(stack, targetStack)) {
					int delta = targetStack.getItem().getMaxCount() - targetStack.getCount();
					delta = Math.min(delta, stackSize);
					stackSize -= delta;
					targetStack.setCount(targetStack.getCount() + delta);
					clickEvents.add(screenHelper.createClickEvent(inventorySlots[j], 0, SlotActionType.PICKUP));
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
				InteractionManager.push(screenHelper.createClickEvent(inventorySlots[i], 0, SlotActionType.PICKUP));
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
		int[] sortIds = new int[slotCount];
		for (int i = 0; i < sortIds.length; i++) {
			sortIds[i] = i;
		}

		sortIds = sortMode.sort(sortIds, stacks, new SortContext(containerScreen, Arrays.asList(inventorySlots)));
		// sortIds now maps the slot index (the target id) to which slot's contents should be moved there (the origin id)
		int[] origin2Target = new int[slotCount];
		for (int i = 0; i < origin2Target.length; i++) {
			origin2Target[sortIds[i]] = i;
		}

		// This is a combined bitset to save whether eac slot is done or empty.
		// It consists of all bits for the done states in the first half and the empty states in the second half.
		BitSet doneSlashEmpty = new BitSet(slotCount * 2);
		for (int i = 0; i < slotCount; i++) { // Iterate all slots to set up the state bit set
			if (i == sortIds[i]) { // If the target slot is equal to the origin,
				doneSlashEmpty.set(i); // then we're done with that slot already.
				continue;
			}
			if (stacks[i].isEmpty()) doneSlashEmpty.set(slotCount + i); // mark if it's empty
		}
		// Iterate all slots, with i as the target slot index
		// sortIds[i] is therefore the origin slot
		for (int i = 0; i < slotCount; i++) {
			if (doneSlashEmpty.get(i)) { // See if we're already done,
				continue; // and skip.
			}
			if (doneSlashEmpty.get(slotCount + sortIds[i])) { // If the origin is empty,
				doneSlashEmpty.set(sortIds[i]); // we can mark it as done
				continue; // and skip.
			}

			// This is where the action happens.
			// Pick up the stack at the origin slot.
			InteractionManager.push(screenHelper.createClickEvent(inventorySlots[sortIds[i]], 0, SlotActionType.PICKUP));
			doneSlashEmpty.set(slotCount + sortIds[i]); // Mark the origin slot as empty (because we picked the stack up, duh)
			currentStack = stacks[sortIds[i]]; // Save the stack we're currently working with
			Slot workingSlot = inventorySlots[sortIds[i]]; // A slot that we can use when fiddling around with swapping stacks
			int id = i; // id will reflect the target slot in the following loop
			do { // This loop follows chained stack moves (e.g. 1->2->5->1).
				if (
						stacks[id].getItem() == currentStack.getItem()
								//&& stacks[id].getCount() == currentStack.getCount()
								&& !doneSlashEmpty.get(slotCount + id)
								&& ItemStack.areTagsEqual(stacks[id], currentStack)
				) {
					// If the current stack and the target stack are completely equal, then we can skip this step in the chain
					if (stacks[id].getCount() == currentStack.getCount()) {
						doneSlashEmpty.set(id); // mark the current target as done
						id = origin2Target[id];
						continue;
					}
					if (currentStack.getCount() < stacks[id].getCount()) { // Clicking with a low stack on a full stack does nothing
						// The workaround is: click working slot, click target slot, click working slot, click target slot, click working slot
						int targetSlotId = inventorySlots[id].id;
						InteractionManager.push(screenHelper.createClickEvent(workingSlot, 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(inventorySlots[targetSlotId], 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(workingSlot, 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(inventorySlots[targetSlotId], 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(workingSlot, 0, SlotActionType.PICKUP));

						currentStack = stacks[id];
						doneSlashEmpty.set(id); // mark the current target as done
						id = origin2Target[id];
						continue;
					}
				}

				// swap the current stack with the target stack
				InteractionManager.push(screenHelper.createClickEvent(inventorySlots[id], 0, SlotActionType.PICKUP));
				currentStack = stacks[id];
				doneSlashEmpty.set(id); // mark the current target as done
				// If the target that we just swapped with was empty before, then this breaks the chain.
				if (doneSlashEmpty.get(slotCount + id)) {
					break;
				}
				id = ArrayUtils.indexOf(sortIds, id); // find the next target (by looking where the current target is set as origin)
			} while (!doneSlashEmpty.get(id)); // If we find a target that is marked as done already, then we can break the chain.
		}
	}
}
