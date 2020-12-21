/*
 * Copyright 2020 Siphalor
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

package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class ContainerScreenHelper<T extends HandledScreen<?>> {
	protected final T screen;
	protected final ClickHandler clickHandler;

	public static int INVALID_SCOPE = Integer.MAX_VALUE;

	public ContainerScreenHelper(T screen, ClickHandler clickHandler) {
		this.screen = screen;
		this.clickHandler = clickHandler;
	}

	public void scroll(Slot referenceSlot, boolean scrollUp) {
		// Shall send determines whether items from the referenceSlot shall be moved to another scope. Otherwise the referenceSlot will receive items.
		boolean shallSend;
		if (MWConfig.scrolling.directionalScrolling) {
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
				for (Slot slot : screen.getScreenHandler().slots) {
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
				for (Slot slot : screen.getScreenHandler().slots) {
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
		if (slot.inventory == null || ((ISlot) slot).mouseWheelie_getInvSlot() >= slot.inventory.size()) {
			return INVALID_SCOPE;
		}
		if (screen instanceof AbstractInventoryScreen) {
			if (slot.inventory instanceof PlayerInventory) {
				if (isHotbarSlot(slot)) {
					return 0;
				} else if (((ISlot) slot).mouseWheelie_getInvSlot() == 40) {
					return -1;
				} else {
					return 1;
				}
			} else {
				return 2;
			}
		} else {
			if (slot.inventory instanceof PlayerInventory) {
				if (MWConfig.general.hotbarScope && isHotbarSlot(slot))
					return -1;
				return 0;
			}
			return 1;
		}
	}

	public void runInScope(int scope, Consumer<Slot> slotConsumer) {
		for (Slot slot : screen.getScreenHandler().slots) {
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

	public interface ClickHandler {
		void handleClick(Slot slot, int data, SlotActionType slotActionType);
	}
}
