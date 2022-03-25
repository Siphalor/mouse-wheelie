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

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.network.ClickEventFactory;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class ContainerScreenHelper<T extends ContainerScreen<?>> {
	protected final T screen;
	protected final ClickEventFactory clickEventFactory;
	protected final IntSet lockedSlots = new IntRBTreeSet();

	public static final int INVALID_SCOPE = Integer.MAX_VALUE;

	protected ContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		this.screen = screen;
		this.clickEventFactory = clickEventFactory;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ContainerScreen<?>> ContainerScreenHelper<T> of(T screen, ClickEventFactory clickEventFactory) {
		if (screen instanceof CreativeInventoryScreen) {
			return (ContainerScreenHelper<T>) new CreativeContainerScreenHelper<>((CreativeInventoryScreen) screen, clickEventFactory);
		}
		return new ContainerScreenHelper<>(screen, clickEventFactory);
	}

	public InteractionManager.InteractionEvent createClickEvent(Slot slot, int action, SlotActionType actionType) {
		if (lockedSlots.contains(slot.id)) {
			return null;
		}
		return clickEventFactory.create(slot, action, actionType);
	}

	public boolean isSlotLocked(Slot slot) {
		return lockedSlots.contains(slot.id);
	}

	public void lockSlot(Slot slot) {
		lockedSlots.add(slot.id);
	}

	public void unlockSlot(Slot slot) {
		lockedSlots.remove(slot.id);
	}

	private InteractionManager.InteractionEvent unlockAfter(InteractionManager.InteractionEvent event, Slot slot) {
		if (event == null) {
			return null;
		}

		return new InteractionManager.CallbackEvent(() -> {
			InteractionManager.Waiter waiter = event.send();
			unlockSlot(slot);
			return waiter;
		});
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
		if (slot.inventory == null || ((ISlot) slot).mouseWheelie_getInvSlot() >= slot.inventory.getInvSize() || !slot.canInsert(ItemStack.EMPTY)) {
			return INVALID_SCOPE;
		}
		if (screen instanceof AbstractInventoryScreen) {
			if (slot.inventory instanceof PlayerInventory) {
				if (isHotbarSlot(slot)) {
					return 0;
				} else if (((ISlot) slot).mouseWheelie_getInvSlot() >= 40) {
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
		for (Slot slot : screen.getContainer().slots) {
			if (getScope(slot) == scope) {
				slotConsumer.accept(slot);
			}
		}
	}

	public void sendSingleItem(Slot slot) {
		if (lockedSlots.contains(slot.id)) {
			return;
		}

		InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.PICKUP));
		InteractionManager.push(clickEventFactory.create(slot, 1, SlotActionType.PICKUP));
		InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.QUICK_MOVE));
		InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.PICKUP));
	}

	public void sendSingleItemLocked(Slot slot) {
		if (lockedSlots.contains(slot.id)) {
			return;
		}

		lockedSlots.add(slot.id);
		InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.PICKUP));
		InteractionManager.push(clickEventFactory.create(slot, 1, SlotActionType.PICKUP));
		InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.QUICK_MOVE));
		InteractionManager.push(unlockAfter(clickEventFactory.create(slot, 0, SlotActionType.PICKUP), slot));
	}

	public void sendStack(Slot slot) {
		InteractionManager.push(createClickEvent(slot, 0, SlotActionType.QUICK_MOVE));
	}

	public void sendStackLocked(Slot slot) {
		if (lockedSlots.contains(slot.id)) {
			return;
		}

		lockedSlots.add(slot.id);
		InteractionManager.push(unlockAfter(clickEventFactory.create(slot, 0, SlotActionType.QUICK_MOVE), slot));
	}

	public void sendAllOfAKind(Slot referenceSlot) {
		ItemStack referenceStack = referenceSlot.getStack().copy();
		runInScope(getScope(referenceSlot), slot -> {
			if (slot.getStack().isItemEqualIgnoreDamage(referenceStack)) {
				sendStack(slot);
			}
		});
	}

	public void sendAllFrom(Slot referenceSlot) {
		runInScope(getScope(referenceSlot), this::sendStack);
	}

	public void dropStack(Slot slot) {
		if (lockedSlots.contains(slot.id)) {
			return;
		}

		InteractionManager.push(createClickEvent(slot, 1, SlotActionType.THROW));
	}

	public void dropStackLocked(Slot slot) {
		if (lockedSlots.contains(slot.id)) {
			return;
		}

		lockedSlots.add(slot.id);
		InteractionManager.push(unlockAfter(clickEventFactory.create(slot, 1, SlotActionType.THROW), slot));
	}

	public void dropAllOfAKind(Slot referenceSlot) {
		ItemStack referenceStack = referenceSlot.getStack().copy();
		runInScope(getScope(referenceSlot), slot -> {
			if (slot.getStack().isItemEqualIgnoreDamage(referenceStack)) {
				dropStack(slot);
			}
		});
	}

	public void dropAllFrom(Slot referenceSlot) {
		runInScope(getScope(referenceSlot), this::dropStack);
	}
}
