/*
 * Copyright 2021 Siphalor
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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
			int scope = getScope(slot);
			for (Slot testSlot : screen.getScreenHandler().slots) {
				if (getScope(testSlot) != scope) {
					ItemStack itemStack = testSlot.getStack();
					if (ItemStack.canCombine(slot.getStack(), itemStack) && itemStack.getCount() < itemStack.getMaxCount()) {
						clickHandler.handleClick(slot, 0, SlotActionType.PICKUP);
						clickHandler.handleClick(testSlot, 0, SlotActionType.PICKUP);
						return;
					}
				}
			}
			for (Slot testSlot : screen.getScreenHandler().slots) {
				if (getScope(testSlot) != scope) {
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
	public int getScope(Slot slot) {
		return slot.inventory instanceof PlayerInventory ? 1 : 0;
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
				} else if (ItemStack.canCombine(itemStack, slot.getStack()) && itemStack.getCount() < itemStack.getMaxCount()) {
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
