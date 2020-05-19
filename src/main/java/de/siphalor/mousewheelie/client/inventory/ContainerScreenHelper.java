package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
public class ContainerScreenHelper<T extends ContainerScreen<?>> {
	protected final T screen;
	protected final ClickHandler clickHandler;

	public ContainerScreenHelper(T screen, ClickHandler clickHandler) {
		this.screen = screen;
		this.clickHandler = clickHandler;
	}

	public void scroll(Slot referenceSlot, boolean scrollUp) {
		boolean shallSend;
		if (Config.directionalScrolling.value) {
			shallSend = shallChangeInventory(referenceSlot, scrollUp);
		} else {
			shallSend = !scrollUp;
		}

		if (shallSend) {
			if (!referenceSlot.canInsert(ItemStack.EMPTY)) {
				sendStack(referenceSlot);
			}
			if (Screen.hasControlDown()) {
				sendAllOfAKind(referenceSlot);
			} else if (Screen.hasShiftDown()) {
				sendStack(referenceSlot);
			} else {
				sendSingleItem(referenceSlot);
			}
		} else {
			ItemStack referenceStack = referenceSlot.getStack().copy();
			if (Screen.hasShiftDown() || Screen.hasControlDown()) {
				for (Slot slot : screen.getContainer().slots) {
					if (slotsInSameScope(slot, referenceSlot)) continue;
					if (slot.getStack().isItemEqualIgnoreDamage(referenceStack)) {
						sendStack(slot);
						if (!Screen.hasControlDown())
							break;
					}
				}
			} else {
				Slot moveSlot = null;
				int stackSize = Integer.MAX_VALUE;
				for (Slot slot : screen.getContainer().slots) {
					if (slotsInSameScope(slot, referenceSlot)) continue;
					if (slot.getStack().isItemEqualIgnoreDamage(referenceStack)) {
						if (slot.getStack().getCount() < stackSize) {
							stackSize = slot.getStack().getCount();
							moveSlot = slot;
							if (stackSize == 1) break;
						}
					}
				}
				if (moveSlot != null)
					sendSingleItem(moveSlot);
			}
		}
	}

	public boolean shallChangeInventory(Slot slot, boolean scrollUp) {
		return isLowerSlot(slot) == scrollUp;
	}

	public boolean isHotbarSlot(Slot slot) {
		return ((ISlot) slot).mouseWheelie_getInvSlot() < 9;
	}

	public boolean isLowerSlot(Slot slot) {
		if (screen instanceof AbstractInventoryScreen) {
			return isHotbarSlot(slot);
		} else {
			return (slot.inventory instanceof PlayerInventory);
		}
	}

	public void sendSingleItem(Slot slot) {
		clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
		clickHandler.handleClick(slot, 1, SlotActionType.PICKUP);
		clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
		clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
	}

	public void sendStack(Slot slot) {
		clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
	}

	public void sendAllOfAKind(Slot referenceSlot) {
		ItemStack referenceStack = referenceSlot.getStack().copy();
		for (Slot slot : screen.getContainer().slots) {
			if (slotsInSameScope(slot, referenceSlot)) {
				if (slot.getStack().isItemEqualIgnoreDamage(referenceStack))
					clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
			}
		}
	}

	public void sendAllFrom(Slot referenceSlot) {
		for (Slot slot : screen.getContainer().slots) {
			if (slotsInSameScope(slot, referenceSlot)) {
				clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
			}
		}
	}

	public boolean slotsInSameScope(Slot slot1, Slot slot2) {
		if (Config.pushHotbarSeparately.value) {
			if (slot1.inventory instanceof PlayerInventory && slot2.inventory instanceof PlayerInventory) {
				return isHotbarSlot(slot1) == isHotbarSlot(slot2);
			}
		}
		return isLowerSlot(slot1) == isLowerSlot(slot2);
	}

	public interface ClickHandler {
		void handleClick(Slot slot, int data, SlotActionType slotActionType);
	}
}
