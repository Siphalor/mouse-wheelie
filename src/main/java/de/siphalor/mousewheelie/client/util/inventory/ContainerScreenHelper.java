package de.siphalor.mousewheelie.client.util.inventory;

import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@SuppressWarnings("WeakerAccess")
public class ContainerScreenHelper {
	private AbstractContainerScreen screen;
	private ClickHandler clickHandler;

	public ContainerScreenHelper(AbstractContainerScreen screen, ClickHandler clickHandler) {
		this.screen = screen;
		this.clickHandler = clickHandler;
	}

	public void scroll(Slot referenceSlot, boolean scrollUp) {
		boolean changeInventory;
		if(screen instanceof InventoryScreen) {
			changeInventory = ((ISlot) referenceSlot).mouseWheelie_getInvSlot() < 9 == scrollUp;
		} else {
			changeInventory = (referenceSlot.inventory instanceof PlayerInventory) == scrollUp;
		}
		if(changeInventory) {
			if(!referenceSlot.canInsert(ItemStack.EMPTY)) {
				clickHandler.handleClick(referenceSlot, 0, SlotActionType.QUICK_MOVE);
			}
			if(Screen.hasControlDown()) {
				sendAllOfAKind(referenceSlot);
			} else if(Screen.hasShiftDown()) {
				clickHandler.handleClick(referenceSlot, 0, SlotActionType.QUICK_MOVE);
			} else {
				sendSingleItem(referenceSlot);
			}
		} else {
			ItemStack referenceStack = referenceSlot.getStack().copy();
			if(Screen.hasShiftDown() || Screen.hasControlDown()) {
				for(Slot slot : screen.getContainer().slotList) {
					if(slotsInSameScope(slot, referenceSlot)) continue;
					if(slot.getStack().isItemEqualIgnoreDamage(referenceStack)) {
						clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
						if(!Screen.hasControlDown())
							break;
					}
				}
			} else {
				Slot moveSlot = null;
				int stackSize = Integer.MAX_VALUE;
				for(Slot slot : screen.getContainer().slotList) {
					if(slotsInSameScope(slot, referenceSlot)) continue;
					if(slot.getStack().isItemEqualIgnoreDamage(referenceStack)) {
						if(slot.getStack().getCount() < stackSize) {
							stackSize = slot.getStack().getCount();
							moveSlot = slot;
							if(stackSize == 1) break;
						}
					}
				}
				if(moveSlot != null)
					sendSingleItem(moveSlot);
			}
		}
	}

	public void sendSingleItem(Slot slot) {
		clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
		clickHandler.handleClick(slot, 1, SlotActionType.PICKUP);
		clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
		clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
	}

	public void sendAllOfAKind(Slot referenceSlot) {
		ItemStack referenceStack = referenceSlot.getStack().copy();
		for(Slot slot : screen.getContainer().slotList) {
			if(slotsInSameScope(slot, referenceSlot)) {
				if(slot.getStack().isItemEqualIgnoreDamage(referenceStack))
					clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
			}
		}
	}

	public void sendAllFrom(Slot referenceSlot) {
		for(Slot slot : screen.getContainer().slotList) {
			if(slotsInSameScope(slot, referenceSlot)) {
				clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
			}
		}
	}

	public boolean slotsInSameScope(Slot slot1, Slot slot2) {
		if(slot1.inventory == slot2.inventory) {
			if(slot1.inventory instanceof PlayerInventory) {
				return (((ISlot) slot1).mouseWheelie_getInvSlot() < 9) == (((ISlot) slot2).mouseWheelie_getInvSlot() < 9);
			}
			return true;
		}
		return false;
	}

	public interface ClickHandler {
		void handleClick(Slot slot, int data, SlotActionType slotActionType);
	}
}
