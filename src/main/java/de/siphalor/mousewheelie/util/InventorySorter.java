package de.siphalor.mousewheelie.util;

import de.siphalor.mousewheelie.Core;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InventorySorter {
	private Container container;
	private List<Slot> inventorySlots;
	private List<ItemStack> stacks;

	public InventorySorter(Container container, Slot originSlot) {
		this.container = container;

		collectSlots(originSlot);

		this.stacks = inventorySlots.stream().map(slot -> slot.getStack().copy()).collect(Collectors.toList());
	}

	private void collectSlots(Slot originSlot) {
		Inventory inventory = originSlot.inventory;
		this.inventorySlots = container.slotList.stream().filter(slot -> slot.inventory == inventory && slot.canInsert(ItemStack.EMPTY)).collect(Collectors.toList());
		if(inventory instanceof PlayerInventory) {
			if(((PlayerInventory) inventory).player.abilities.creativeMode && MinecraftClient.getInstance().currentScreen instanceof CreativePlayerInventoryScreen) {
				// Mojang's creative inventory/slot/container code is so messed up, I really can't sort this out for the player creative inventory
				// TODO some day this should get fixed though
				inventorySlots = Collections.emptyList();
			}

			final int originInvSlot = ((ISlot) originSlot).mouseWheelie_getInvSlot();
			final boolean originInHotbar = originInvSlot >= 0 && originInvSlot < 9;
			final int offHandInvSlot = inventory.getInvSize() - 1;
			final boolean originOffHand = originInvSlot == offHandInvSlot;
			inventorySlots = inventorySlots.stream().filter(slot -> {
				final int invSlot = ((ISlot) slot).mouseWheelie_getInvSlot();
				return (invSlot >= 0 && invSlot < 9) == originInHotbar && (invSlot == offHandInvSlot) == originOffHand;
			}).collect(Collectors.toList());
		}
	}

	public void combineStacks() {
		ItemStack stack;
		ArrayDeque<Core.ClickEvent> clickEvents = new ArrayDeque<>();
		for(int i = stacks.size() - 1; i >= 0; i--) {
			stack = stacks.get(i);
			if(stack.isEmpty()) continue;
			int stackSize = stack.getAmount();
			if(stackSize >= stack.getItem().getMaxAmount()) continue;
			clickEvents.add(new Core.ClickEvent(container.syncId, inventorySlots.get(i).id, 0, SlotActionType.PICKUP));
			for(int j = 0; j < i; j++) {
				ItemStack targetStack = stacks.get(j);
				if(targetStack.isEmpty()) continue;
				if(targetStack.getAmount() >= targetStack.getItem().getMaxAmount()) continue;
				if(stack.getItem() == targetStack.getItem() && ItemStack.areTagsEqual(stack, targetStack)) {
					int delta = targetStack.getItem().getMaxAmount() - targetStack.getAmount();
					delta = Math.min(delta, stackSize);
					stackSize -= delta;
					targetStack.setAmount(targetStack.getAmount() + delta);
					clickEvents.add(new Core.ClickEvent(container.syncId, inventorySlots.get(j).id, 0, SlotActionType.PICKUP));
					if(stackSize <= 0) break;
				}
			}
			if(clickEvents.size() <= 1) {
				clickEvents.clear();
				continue;
			}
			Core.interactionEventQueue.addAll(clickEvents);
			Core.triggerSend();
			clickEvents.clear();
			if(stackSize > 0) {
				Core.pushClickEvent(container.syncId, inventorySlots.get(i).id, 0, SlotActionType.PICKUP);
				stack.setAmount(stackSize);
			} else {
				stacks.set(i, ItemStack.EMPTY);
			}
		}
	}

	public void sort(SortMode sortMode) {
		combineStacks();
		final int slotCount = stacks.size();
		List<Integer> sortIds = IntStream.range(0, slotCount).boxed().collect(Collectors.toList());
		switch(sortMode) {
			case ALPHABET:
				List<String> strings = sortIds.stream().map(id -> {
					ItemStack itemStack = stacks.get(id);
					if (itemStack.isEmpty()) return "";
					return I18n.translate(itemStack.getTranslationKey());
				}).collect(Collectors.toList());
				sortIds.sort((o1, o2) -> {
					if (strings.get(o1).equals("")) {
						if (strings.get(o2).equals(""))
							return 0;
						return 1;
					}
					if (strings.get(o2).equals("")) return -1;
					int comp = strings.get(o1).compareToIgnoreCase(strings.get(o2));
					if (comp == 0) {
						return Integer.compare(stacks.get(o2).getAmount(), stacks.get(o1).getAmount());
					}
					return comp;
				});
				break;
			case QUANTITY:
				HashMap<Item, HashMap<CompoundTag, Integer>> itemToAmountMap = new HashMap<>();
				for(ItemStack stack : stacks) {
					if (stack.isEmpty()) continue;
					if (!itemToAmountMap.containsKey(stack.getItem())) {
						HashMap<CompoundTag, Integer> newMap = new HashMap<>();
						newMap.put(stack.getOrCreateTag(), stack.getAmount());
						itemToAmountMap.put(stack.getItem(), newMap);
					} else {
						HashMap<CompoundTag, Integer> itemMap = itemToAmountMap.get(stack.getItem());
						if (!itemMap.containsKey(stack.getOrCreateTag())) {
							itemMap.put(stack.getTag(), stack.getAmount());
						} else {
							itemMap.replace(stack.getTag(), itemMap.get(stack.getTag()) + stack.getAmount());
						}
					}
				}
				sortIds.sort((o1, o2) -> {
					ItemStack stack = stacks.get(o1);
					ItemStack stack2 = stacks.get(o2);
					if (stack.isEmpty()) {
						return stack2.isEmpty() ? 0 : 1;
					}
					if (stack2.isEmpty()) {
						return -1;
					}
					Integer a = itemToAmountMap.get(stack.getItem()).get(stack.getTag());
					Integer a2 = itemToAmountMap.get(stack2.getItem()).get(stack2.getTag());
					return Integer.compare(a2, a);
				});
				break;
			case RAWID:
				Integer[] rawIds = inventorySlots.stream().map(slot -> (Integer) (slot.getStack().isEmpty() ? Integer.MAX_VALUE : Registry.ITEM.getRawId(slot.getStack().getItem()))).toArray(Integer[]::new);
				sortIds.sort(Comparator.comparingInt(o -> rawIds[o]));
				break;
		}
		BitSet doneSlashEmpty = new BitSet(slotCount * 2);
		for(int i = 0; i < slotCount; i++) {
			if(stacks.get(i).isEmpty()) doneSlashEmpty.set(slotCount + i);
		}
		for(int i = 0; i < slotCount; i++) {
			if(doneSlashEmpty.get(i) || doneSlashEmpty.get(sortIds.get(i))) {
				continue;
			}
			if(doneSlashEmpty.get(slotCount + i)) {
				doneSlashEmpty.set(slotCount + sortIds.get(i));
			}
			if(i == sortIds.get(i)) {
				doneSlashEmpty.set(i);
				continue;
			}
			Core.pushClickEvent(container.syncId, inventorySlots.get(sortIds.get(i)).id, 0, SlotActionType.PICKUP);
			doneSlashEmpty.clear(slotCount + sortIds.get(i));
			int id = i;
			while(!doneSlashEmpty.get(id)) {
				Core.pushClickEvent(container.syncId, inventorySlots.get(id).id, 0, SlotActionType.PICKUP);
				doneSlashEmpty.set(id);
				if(doneSlashEmpty.get(slotCount + id)) {
					doneSlashEmpty.set(slotCount + id);
					break;
				}
				id = sortIds.indexOf(id);
			}
		}
	}
}
