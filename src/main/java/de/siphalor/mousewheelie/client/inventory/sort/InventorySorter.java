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
	private HandledScreen<?> containerScreen;
	private List<Slot> inventorySlots;
	private ItemStack[] stacks;

	public InventorySorter(HandledScreen<?> containerScreen, Slot originSlot) {
		this.containerScreen = containerScreen;

		collectSlots(originSlot);

		this.stacks = inventorySlots.stream().map(slot -> slot.getStack().copy()).toArray(ItemStack[]::new);
	}

	private void collectSlots(Slot originSlot) {
		ContainerScreenHelper<? extends HandledScreen<?>> screenHelper = new ContainerScreenHelper<>(containerScreen, (slot, data, slotActionType) -> {
		});
		int originScope = screenHelper.getScope(originSlot);
		if (originScope == ContainerScreenHelper.INVALID_SCOPE) {
			return;
		}
		inventorySlots = new ArrayList<>();
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
		final int slotCount = stacks.length;
		Integer[] sortIds = IntStream.range(0, slotCount).boxed().toArray(Integer[]::new);

		sortIds = sortMode.sort(sortIds, stacks);

		BitSet doneSlashEmpty = new BitSet(slotCount * 2);
		for (int i = 0; i < slotCount; i++) {
			if (stacks[i].isEmpty()) doneSlashEmpty.set(slotCount + i);
		}
		for (int i = 0; i < slotCount; i++) {
			if (doneSlashEmpty.get(i) || doneSlashEmpty.get(sortIds[i])) {
				continue;
			}
			if (doneSlashEmpty.get(slotCount + i)) {
				doneSlashEmpty.set(slotCount + sortIds[i]);
			}
			if (i == sortIds[i]) {
				doneSlashEmpty.set(i);
				continue;
			}
			InteractionManager.pushClickEvent(containerScreen.getScreenHandler().syncId, inventorySlots.get(sortIds[i]).id, 0, SlotActionType.PICKUP);
			doneSlashEmpty.clear(slotCount + sortIds[i]);
			int id = i;
			while (!doneSlashEmpty.get(id)) {
				InteractionManager.pushClickEvent(containerScreen.getScreenHandler().syncId, inventorySlots.get(id).id, 0, SlotActionType.PICKUP);
				doneSlashEmpty.set(id);
				if (doneSlashEmpty.get(slotCount + id)) {
					doneSlashEmpty.set(slotCount + id);
					break;
				}
				id = ArrayUtils.indexOf(sortIds, id);
			}
		}
	}
}
