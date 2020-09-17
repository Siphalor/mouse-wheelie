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

import java.util.function.Consumer;

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
		// Shall send determines whether items from the referenceSlot shall be moved to another scope. Otherwise the referenceSlot will receive items.
		boolean shallSend;
		if (Config.directionalScrolling.value) {
			shallSend = shallChangeInventory(referenceSlot, scrollUp);
		} else {
			shallSend = !scrollUp;
			scrollUp = false;
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
			int referenceScope = getScope(referenceSlot);
			if (Screen.hasShiftDown() || Screen.hasControlDown()) {
				for (Slot slot : screen.getContainer().slots) {
					if (getScope(slot) == referenceScope) continue;
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
					if (getScope(slot) == referenceScope) continue;
					if (getScope(slot) <= 0 == scrollUp) {
						if (slot.getStack().isItemEqualIgnoreDamage(referenceStack)) {
							if (slot.getStack().getCount() < stackSize) {
								stackSize = slot.getStack().getCount();
								moveSlot = slot;
								if (stackSize == 1) break;
							}
						}
					}
				}
				if (moveSlot != null)
					sendSingleItem(moveSlot);
			}
		}
	}

	public boolean shallChangeInventory(Slot slot, boolean scrollUp) {
		return (getScope(slot) <= 0) == scrollUp;
	}

	public boolean isHotbarSlot(Slot slot) {
		return ((ISlot) slot).mouseWheelie_getInvSlot() < 9;
	}

	public int getScope(Slot slot) {
		if (slot.inventory == null || ((ISlot) slot).mouseWheelie_getInvSlot() >= slot.inventory.getInvSize()) {
			return Integer.MAX_VALUE;
		}
		if (screen instanceof AbstractInventoryScreen) {
			if (slot.inventory instanceof PlayerInventory) {
				if (isHotbarSlot(slot)) {
					return 0;
				} else {
					return 1;
				}
			} else {
				return 2;
			}
		} else {
			if (slot.inventory instanceof PlayerInventory) {
				if (Config.pushHotbarSeparately.value && isHotbarSlot(slot))
					return -1;
				return 0;
			}
			return 1;
		}
	}

	public void runInScope(int scope, Consumer<Slot> slotConsumer) {
		for (Slot slot : screen.getContainer().slots) {
			if (getScope(slot) == scope) {
				slotConsumer.accept(slot);
			}
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
		runInScope(getScope(referenceSlot), slot -> {
			if (slot.getStack().isItemEqualIgnoreDamage(referenceStack))
				clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
		});
	}

	public void sendAllFrom(Slot referenceSlot) {
		runInScope(getScope(referenceSlot), slot -> {
			clickHandler.handleClick(slot, 0, SlotActionType.QUICK_MOVE);
		});
	}

	public void dropAllOfAKind(Slot referenceSlot) {
		ItemStack referenceStack = referenceSlot.getStack().copy();
		runInScope(getScope(referenceSlot), slot -> {
			if (slot.getStack().isItemEqualIgnoreDamage(referenceStack))
				clickHandler.handleClick(slot, 1, SlotActionType.THROW);
		});
	}

	public void dropAllFrom(Slot referenceSlot) {
		runInScope(getScope(referenceSlot), slot -> {
			clickHandler.handleClick(slot, 1, SlotActionType.THROW);
		});
	}

	public interface ClickHandler {
		void handleClick(Slot slot, int data, SlotActionType slotActionType);
	}
}
