package de.siphalor.mousewheelie.client.inventory;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class CreativeContainerScreenHelper<T extends CreativeInventoryScreen> extends ContainerScreenHelper<T> {
	public CreativeContainerScreenHelper(T screen, ClickHandler clickHandler) {
		super(screen, clickHandler);
	}

	@Override
	public void sendSingleItem(Slot slot) {
		if (slot.inventory instanceof PlayerInventory) {
			super.sendSingleItem(slot);
		} else {
			for (Slot testSlot : screen.getContainer().slotList) {
				if (!slotsInSameScope(slot, testSlot)) {
					ItemStack itemStack = testSlot.getStack();
					if (Container.canStacksCombine(slot.getStack(), itemStack) && itemStack.getCount() < itemStack.getMaxCount()) {
						clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
						clickHandler.handleClick(testSlot, 0, SlotActionType.PICKUP);
						return;
					}
				}
			}
			for (Slot testSlot : screen.getContainer().slotList) {
				if (!slotsInSameScope(slot, testSlot)) {
					if (!testSlot.hasStack()) {
						clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
						clickHandler.handleClick(testSlot, 0, SlotActionType.PICKUP);
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean shallChangeInventory(Slot slot, boolean scrollUp) {
		return slot.inventory instanceof PlayerInventory == scrollUp;
	}

	@Override
	public void sendStack(Slot slot) {
		if (slot.inventory instanceof PlayerInventory) {
			super.sendStack(slot);
		} else {
			int count = slot.getStack().getMaxCount();
			clickHandler.handleClick(slot, 0, SlotActionType.CLONE);
			for (Slot testSlot : screen.getContainer().slotList) {
				ItemStack itemStack = testSlot.getStack();
				if (itemStack.isEmpty()) {
					clickHandler.handleClick(testSlot, 0, SlotActionType.PICKUP);
					return;
				} else if (Container.canStacksCombine(itemStack, slot.getStack()) && itemStack.getCount() < itemStack.getMaxCount()) {
					count -= itemStack.getCount();
					clickHandler.handleClick(testSlot, 0, SlotActionType.PICKUP);
					if (count <= 0) return;
				}
			}
			clickHandler.handleClick(getDelSlot(slot.getStack()), 0, SlotActionType.PICKUP);
		}
	}

	@Override
	public void sendAllOfAKind(Slot referenceSlot) {
		if (referenceSlot.inventory instanceof PlayerInventory) {
			super.sendAllOfAKind(referenceSlot);
		} else {
			sendStack(referenceSlot);
		}
	}

	@Override
	public void sendAllFrom(Slot referenceSlot) {
		if (referenceSlot.inventory instanceof PlayerInventory) {
			super.sendAllFrom(referenceSlot);
		}
	}

	private Slot getDelSlot(ItemStack delStack) {
		for (Slot slot : screen.getContainer().slotList) {
			if (slot.getStack().getItem() != delStack.getItem()) {
				return slot;
			}
		}
		return screen.getContainer().slotList.get(0);
	}

	@Override
	public boolean slotsInSameScope(Slot slot1, Slot slot2) {
		return slot1.inventory == slot2.inventory;
	}
}
