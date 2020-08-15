package de.siphalor.mousewheelie.client.inventory.sort;

import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class InventorySorter {
	private ScreenHandler container;
	private List<Slot> inventorySlots;
	private ItemStack[] stacks;

	public InventorySorter(ScreenHandler container, Slot originSlot) {
		this.container = container;

		collectSlots(originSlot);

		this.stacks = inventorySlots.stream().map(slot -> slot.getStack().copy()).toArray(ItemStack[]::new);
	}

	private void collectSlots(Slot originSlot) {
		Inventory inventory = originSlot.inventory;
		this.inventorySlots = container.slots.stream().filter(slot -> slot.inventory == inventory && slot.canInsert(ItemStack.EMPTY)).collect(Collectors.toList());
		if (inventory instanceof PlayerInventory) {
			if (((PlayerInventory) inventory).player.abilities.creativeMode && MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen) {
				// Mojang's creative inventory/slot/container code is so messed up, I really can't sort this out for the player creative inventory
				// TODO some day this should get fixed though
				inventorySlots = Collections.emptyList();
			}

			final int originInvSlot = ((ISlot) originSlot).mouseWheelie_getInvSlot();
			final boolean originInHotbar = originInvSlot >= 0 && originInvSlot < 9;
			final int offHandInvSlot = inventory.size() - 1;
			final boolean originOffHand = originInvSlot == offHandInvSlot;
			inventorySlots = inventorySlots.stream().filter(slot -> {
				final int invSlot = ((ISlot) slot).mouseWheelie_getInvSlot();
				return (invSlot >= 0 && invSlot < 9) == originInHotbar && (invSlot == offHandInvSlot) == originOffHand;
			}).collect(Collectors.toList());
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
			clickEvents.add(new InteractionManager.ClickEvent(container.syncId, inventorySlots.get(i).id, 0, SlotActionType.PICKUP));
			for (int j = 0; j < i; j++) {
				ItemStack targetStack = stacks[j];
				if (targetStack.isEmpty()) continue;
				if (targetStack.getCount() >= targetStack.getItem().getMaxCount()) continue;
				if (stack.getItem() == targetStack.getItem() && ItemStack.areTagsEqual(stack, targetStack)) {
					int delta = targetStack.getItem().getMaxCount() - targetStack.getCount();
					delta = Math.min(delta, stackSize);
					stackSize -= delta;
					targetStack.setCount(targetStack.getCount() + delta);
					clickEvents.add(new InteractionManager.ClickEvent(container.syncId, inventorySlots.get(j).id, 0, SlotActionType.PICKUP));
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
				InteractionManager.pushClickEvent(container.syncId, inventorySlots.get(i).id, 0, SlotActionType.PICKUP);
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
			InteractionManager.pushClickEvent(container.syncId, inventorySlots.get(sortIds[i]).id, 0, SlotActionType.PICKUP);
			doneSlashEmpty.clear(slotCount + sortIds[i]);
			int id = i;
			while (!doneSlashEmpty.get(id)) {
				InteractionManager.pushClickEvent(container.syncId, inventorySlots.get(id).id, 0, SlotActionType.PICKUP);
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
