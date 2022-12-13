/*
 * Copyright 2020-2022 Siphalor
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

import de.siphalor.mousewheelie.client.network.ClickEventFactory;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class CreativeContainerScreenHelper<T extends CreativeInventoryScreen> extends ContainerScreenHelper<T> {
	public CreativeContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		super(screen, clickEventFactory);
	}

	@Override
	public void sendSingleItem(Slot slot) {
		if (slot.inventory instanceof PlayerInventory) {
			super.sendSingleItem(slot);
		} else {
			int scope = getScope(slot);
			for (Slot testSlot : screen.getContainer().slots) {
				if (getScope(testSlot) != scope) {
					ItemStack itemStack = testSlot.getStack();
					if (Container.canStacksCombine(slot.getStack(), itemStack) && itemStack.getCount() < itemStack.getMaxCount()) {
						InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.PICKUP));
						InteractionManager.push(clickEventFactory.create(testSlot, 0, SlotActionType.PICKUP));
						return;
					}
				}
			}
			for (Slot testSlot : screen.getContainer().slots) {
				if (getScope(testSlot) != scope) {
					if (!testSlot.hasStack()) {
						InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.PICKUP));
						InteractionManager.push(clickEventFactory.create(testSlot, 0, SlotActionType.PICKUP));
						return;
					}
				}
			}
		}
	}

	@Override
	public int getScope(Slot slot, boolean forceSmallerScopes) {
		if (screen.method_2469() == ItemGroup.INVENTORY.getIndex()) {
			return super.getScope(slot, forceSmallerScopes);
		}
		if (slot.inventory instanceof PlayerInventory) {
			if (isHotbarSlot(slot)) {
				return 0;
			}
		}
		return INVALID_SCOPE;
	}

	@Override
	public void sendStack(Slot slot) {
		if (slot.inventory instanceof PlayerInventory) {
			super.sendStack(slot);
		} else {
			int count = slot.getStack().getMaxCount();
			InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.CLONE));
			for (Slot testSlot : screen.getContainer().slots) {
				ItemStack itemStack = testSlot.getStack();
				if (itemStack.isEmpty()) {
					InteractionManager.push(clickEventFactory.create(testSlot, 0, SlotActionType.PICKUP));
					return;
				} else if (Container.canStacksCombine(itemStack, slot.getStack()) && itemStack.getCount() < itemStack.getMaxCount()) {
					count -= itemStack.getCount();
					InteractionManager.push(clickEventFactory.create(testSlot, 0, SlotActionType.PICKUP));
					if (count <= 0) return;
				}
			}
			InteractionManager.push(clickEventFactory.create(getDelSlot(slot.getStack()), 0, SlotActionType.PICKUP));
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
		for (Slot slot : screen.getContainer().slots) {
			if (slot.getStack().getItem() != delStack.getItem()) {
				return slot;
			}
		}
		return screen.getContainer().slots.get(0);
	}
}
