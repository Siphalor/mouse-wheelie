package de.siphalor.mousewheelie.client.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
public class CreativeContainerScreenHelper<T extends CreativeInventoryScreen> extends ContainerScreenHelper<T> {
	public CreativeContainerScreenHelper(T screen, ClickHandler clickHandler) {
		super(screen, clickHandler);
	}

	@Override
	public void sendSingleItem(Slot slot) {
		if (slot.inventory instanceof PlayerInventory) {
			super.sendSingleItem(slot);
		} else {
			for (Slot testSlot : screen.getScreenHandler().slots) {
				if (!slotsInSameScope(slot, testSlot)) {
					ItemStack itemStack = testSlot.getStack();
					if (ScreenHandler.canStacksCombine(slot.getStack(), itemStack) && itemStack.getCount() < itemStack.getMaxCount()) {
						clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
						clickHandler.handleClick(testSlot, 0, SlotActionType.PICKUP);
						return;
					}
				}
			}
			for (Slot testSlot : screen.getScreenHandler().slots) {
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
	public boolean isLowerSlot(Slot slot) {
		return slot.inventory instanceof PlayerInventory;
	}

	@Override
	public void sendStack(Slot slot) {
		if (slot.inventory instanceof PlayerInventory) {
			super.sendStack(slot);
		} else {
			int count = slot.getStack().getMaxCount();
			clickHandler.handleClick(slot, 0, SlotActionType.CLONE);
			for (Slot testSlot : screen.getScreenHandler().slots) {
				ItemStack itemStack = testSlot.getStack();
				if (itemStack.isEmpty()) {
					clickHandler.handleClick(testSlot, 0, SlotActionType.PICKUP);
					return;
				} else if (ScreenHandler.canStacksCombine(itemStack, slot.getStack()) && itemStack.getCount() < itemStack.getMaxCount()) {
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
		for (Slot slot : screen.getScreenHandler().slots) {
			if (slot.getStack().getItem() != delStack.getItem()) {
				return slot;
			}
		}
		return screen.getScreenHandler().slots.get(0);
	}
}
