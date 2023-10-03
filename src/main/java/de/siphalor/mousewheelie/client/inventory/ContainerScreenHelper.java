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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.network.ClickEventFactory;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.mousewheelie.client.util.ReverseIterator;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class ContainerScreenHelper<T extends ContainerScreen<?>> {
	protected final T screen;
	protected final ClickEventFactory clickEventFactory;
	protected final ReadWriteLock slotStatesLock = new ReentrantReadWriteLock();
	protected final Int2ObjectMap<SlotInteractionState> slotStates;

	public static final int INVALID_SCOPE = Integer.MAX_VALUE;

	protected ContainerScreenHelper(T screen, ClickEventFactory clickEventFactory) {
		this.screen = screen;
		this.clickEventFactory = clickEventFactory;
		this.slotStates = new Int2ObjectArrayMap<>(10);
	}

	@SuppressWarnings("unchecked")
	public static <T extends ContainerScreen<?>> ContainerScreenHelper<T> of(T screen, ClickEventFactory clickEventFactory) {
		if (screen instanceof CreativeInventoryScreen) {
			return (ContainerScreenHelper<T>) new CreativeContainerScreenHelper<>((CreativeInventoryScreen) screen, clickEventFactory);
		}
		return new ContainerScreenHelper<>(screen, clickEventFactory);
	}

	public InteractionManager.InteractionEvent createClickEvent(Slot slot, int action, SlotActionType actionType) {
		if (getSlotState(slot).areInteractionsLocked()) {
			return null;
		}
		return clickEventFactory.create(slot, action, actionType);
	}

	public SlotInteractionState getSlotState(Slot slot) {
		Lock readLock = slotStatesLock.readLock();
		readLock.lock();
		try {
			SlotInteractionState state = slotStates.get(((ISlot) slot).mouseWheelie_getIdInContainer());
			if (state == null) {
				return SlotInteractionState.NORMAL;
			}
			return state;
		} finally {
			readLock.unlock();
		}
	}

	public void setSlotState(Slot slot, SlotInteractionState state) {
		Lock writeLock = slotStatesLock.writeLock();
		writeLock.lock();
		try {
			int slotId = ((ISlot) slot).mouseWheelie_getIdInContainer();
			if (state == SlotInteractionState.NORMAL) {
				slotStates.remove(slotId);
			} else {
				slotStates.put(slotId, state);
			}
		} finally {
			writeLock.unlock();
		}
	}

	public void unlockSlot(Slot slot) {
		setSlotState(slot, SlotInteractionState.NORMAL);
	}

	private InteractionManager.InteractionEvent lockBefore(InteractionManager.InteractionEvent event, Slot slot, SlotInteractionState slotState) {
		if (event == null) {
			return null;
		}

		return new InteractionManager.CallbackEvent(() -> {
			setSlotState(slot, slotState);
			return event.send();
		}, event.shouldRunOnMainThread());
	}

	private InteractionManager.InteractionEvent unlockAfter(InteractionManager.InteractionEvent event, Slot slot) {
		if (event == null) {
			return null;
		}

		return new InteractionManager.CallbackEvent(() -> {
			InteractionManager.Waiter waiter = event.send();
			unlockSlot(slot);
			return waiter;
		}, event.shouldRunOnMainThread());
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
			// If deposit modifier and restock modifier are equal, deposit modifier takes precedence
			if (MWClient.DEPOSIT_MODIFIER.isPressed()) {
				depositAllFrom(referenceSlot);
				return;
			}
			if (MWClient.RESTOCK_MODIFIER.isPressed()) {
				restockAll(getComplementaryScope(getScope(referenceSlot)));
				return;
			}

			if (!referenceSlot.canInsert(ItemStack.EMPTY)) {
				sendStack(referenceSlot);
			}
			if (MWClient.ALL_OF_KIND_MODIFIER.isPressed()) {
				sendAllOfAKind(referenceSlot);
			} else if (MWClient.WHOLE_STACK_MODIFIER.isPressed()) {
				sendStack(referenceSlot);
			} else {
				sendSingleItem(referenceSlot);
			}
		} else {
			// If deposit modifier and restock modifier are equal, restock modifier takes precedence
			if (MWClient.RESTOCK_MODIFIER.isPressed()) {
				if (MWClient.WHOLE_STACK_MODIFIER.isPressed()) {
					restockAll(referenceSlot);
				} else {
					restockAllOfAKind(referenceSlot);
				}
				return;
			}
			if (MWClient.DEPOSIT_MODIFIER.isPressed()) {
				depositAllFrom(getComplementaryScope(getScope(referenceSlot)));
				return;
			}

			ItemStack referenceStack = referenceSlot.getStack().copy();
			int referenceScope = getScope(referenceSlot);
			boolean wholeStackModifier = MWClient.WHOLE_STACK_MODIFIER.isPressed();
			boolean allOfKindModifier = MWClient.ALL_OF_KIND_MODIFIER.isPressed();
			if (wholeStackModifier || allOfKindModifier) {
				for (Slot slot : screen.getContainer().slots) {
					if (getScope(slot) == referenceScope) continue;
					if (ItemStackUtils.areItemsOfSameKind(slot.getStack(), referenceStack)) {
						sendStack(slot);
						if (!allOfKindModifier) {
							break;
						}
					}
				}
			} else {
				Slot moveSlot = null;
				int stackSize = Integer.MAX_VALUE;
				for (Slot slot : screen.getContainer().slots) {
					if (getScope(slot) == referenceScope) continue;
					if (getScope(slot) <= 0 == scrollUp) {
						if (ItemStackUtils.areItemsOfSameKind(slot.getStack(), referenceStack)) {
							if (slot.getStack().getCount() < stackSize) {
								stackSize = slot.getStack().getCount();
								moveSlot = slot;
								if (stackSize == 1) {
									break;
								}
							}
						}
					}
				}
				if (moveSlot != null) {
					sendSingleItem(moveSlot);
				}
			}
		}
	}

	public boolean shallChangeInventory(Slot slot, boolean scrollUp) {
		return (getScope(slot) <= 0) == scrollUp;
	}

	public boolean isHotbarSlot(Slot slot) {
		return ((ISlot) slot).mouseWheelie_getIndexInInv() < 9;
	}

	public int getScope(Slot slot) {
		return getScope(slot, false);
	}

	public int getScope(Slot slot, boolean preferSmallerScopes) {
		if (slot.inventory == null || ((ISlot) slot).mouseWheelie_getIndexInInv() >= slot.inventory.getInvSize() || !slot.canInsert(ItemStack.EMPTY)) {
			return INVALID_SCOPE;
		}
		if (screen instanceof AbstractInventoryScreen) {
			if (slot.inventory instanceof PlayerInventory) {
				if (isHotbarSlot(slot)) {
					return 0;
				} else if (((ISlot) slot).mouseWheelie_getIndexInInv() >= 40) {
					return -1;
				} else {
					return 1;
				}
			} else {
				return 2;
			}
		} else {
			if (slot.inventory instanceof PlayerInventory) {
				if (isHotbarSlot(slot)) {
					if (MWConfig.general.hotbarScoping == MWConfig.General.HotbarScoping.HARD
							|| MWConfig.general.hotbarScoping == MWConfig.General.HotbarScoping.SOFT && preferSmallerScopes) {
						return -1;
					}
				}
				return 0;
			}
			return 1;
		}
	}

	public void runInScope(int scope, Consumer<Slot> slotConsumer) {
		runInScope(scope, false, slotConsumer);
	}

	public void runInScope(int scope, boolean preferSmallerScopes, Consumer<Slot> slotConsumer) {
		for (Slot slot : screen.getContainer().slots) {
			if (getScope(slot, preferSmallerScopes) == scope) {
				slotConsumer.accept(slot);
			}
		}
	}

	public int getComplementaryScope(int scope) {
		if (scope <= 0) {
			return 1;
		}
		return 0;
	}

	public void sendSingleItem(Slot slot) {
		SlotInteractionState slotState = getSlotState(slot);
		if (slotState.areInteractionsLocked()) {
			return;
		}

		if (slotState.isAmountStable() && slot.getStack().getCount() == 1) {
			InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.QUICK_MOVE));
			return;
		}
		InteractionManager.push(lockBefore(clickEventFactory.create(slot, 0, SlotActionType.PICKUP), slot, SlotInteractionState.UNSTABLE_AMOUNT));
		InteractionManager.push(clickEventFactory.create(slot, 1, SlotActionType.PICKUP));
		InteractionManager.push(clickEventFactory.create(slot, 0, SlotActionType.QUICK_MOVE));
		InteractionManager.push(unlockAfter(clickEventFactory.create(slot, 0, SlotActionType.PICKUP), slot));
	}

	public void sendStack(Slot slot) {
		InteractionManager.push(createClickEvent(slot, 0, SlotActionType.QUICK_MOVE));
	}

	public void sendStackLocked(Slot slot) {
		if (getSlotState(slot).areInteractionsLocked()) {
			return;
		}

		setSlotState(slot, SlotInteractionState.TEMP_LOCKED);
		InteractionManager.push(unlockAfter(clickEventFactory.create(slot, 0, SlotActionType.QUICK_MOVE), slot));
	}

	public void sendAllOfAKind(Slot referenceSlot) {
		ItemStack stack = referenceSlot.getStack();
		if (stack.isEmpty()) {
			return;
		}

		ItemStack referenceStack = stack.copy();
		runInScope(getScope(referenceSlot), slot -> {
			if (ItemStackUtils.areItemsOfSameKind(slot.getStack(), referenceStack)) {
				sendStack(slot);
			}
		});
	}

	public void sendAllFrom(Slot referenceSlot) {
		runInScope(getScope(referenceSlot, true), true, this::sendStack);
	}

	public void depositAllFrom(Slot referenceSlot) {
		depositAllFrom(getScope(referenceSlot, false));
	}

	public void depositAllFrom(int scope) {
		int complementaryScope = getComplementaryScope(scope);

		Set<ItemKind> itemKinds = new HashSet<>();
		runInScope(complementaryScope, slot -> {
			if (slot.hasStack()) {
				itemKinds.add(ItemKind.of(slot.getStack()));
			}
		});

		runInScope(scope, slot -> {
			if (slot.hasStack() && itemKinds.contains(ItemKind.of(slot.getStack()))) {
				sendStackLocked(slot);

			}
		});
	}

	public void restockAllOfAKind(Slot referenceSlot) {
		ItemStack referenceStack = referenceSlot.getStack();
		if (referenceStack.isEmpty()) {
			return;
		}

		int scope = getScope(referenceSlot, true);
		int complementaryScope = getComplementaryScope(scope);
		restockAllOfAKind(
				screen.getContainer().slots.stream()
						.filter(slot -> getScope(slot, true) == scope && ItemStackUtils.areItemsOfSameKind(slot.getStack(), referenceStack))
						.iterator(),
				complementaryScope
		);
	}

	private void restockAllOfAKind(Iterator<Slot> targetSlots, int complementaryScope) {
		Iterator<Slot> takeSlots = ReverseIterator.of(screen.getContainer().slots);
		Slot currentTakeSlot = null;
		int currentTakeCount = 0;

		while (targetSlots.hasNext()) {
			Slot targetSlot = targetSlots.next();
			ItemStack targetStack = targetSlot.getStack();
			int space = targetStack.getMaxCount() - targetStack.getCount();

			while (space > 0) {
				if (currentTakeCount == 0) {
					while (true) {
						if (!takeSlots.hasNext()) {
							return;
						}

						currentTakeSlot = takeSlots.next();
						if (getScope(currentTakeSlot, false) != complementaryScope) {
							continue;
						}

						ItemStack currentTakeStack = currentTakeSlot.getStack();
						currentTakeCount = currentTakeStack.getCount();

						if (currentTakeCount <= 0) {
							continue;
						}
						if (ItemStackUtils.areItemsOfSameKind(currentTakeStack, targetStack)) {
							break;
						}
					}
					InteractionManager.push(clickEventFactory.create(currentTakeSlot, 0, SlotActionType.PICKUP));
				}

				InteractionManager.push(clickEventFactory.create(targetSlot, 0, SlotActionType.PICKUP));
				space -= currentTakeCount;

				if (space <= 0) {
					currentTakeCount = -space;
					continue;
				}
				currentTakeCount = 0;
			}
		}

		if (currentTakeCount > 0) {
			InteractionManager.push(clickEventFactory.create(currentTakeSlot, 0, SlotActionType.PICKUP));
		}
	}

	public void restockAll(Slot referenceSlot) {
		restockAll(getScope(referenceSlot, false));
	}

	public void restockAll(int scope) {
		ListMultimap<ItemKind, Slot> slotsByItemKind = ArrayListMultimap.create();
		runInScope(scope, slot -> {
			ItemStack stack = slot.getStack();
			int count = stack.getCount();
			if (count > 0 && count < stack.getMaxCount()) {
				slotsByItemKind.put(ItemKind.of(stack), slot);
			}
		});
		int complementaryScope = getComplementaryScope(scope);

		slotsByItemKind.asMap().forEach((itemKind, slots) ->
				restockAllOfAKind(slots.iterator(), complementaryScope)
		);
	}

	public void dropStack(Slot slot) {
		if (getSlotState(slot).areInteractionsLocked()) {
			return;
		}

		InteractionManager.push(createClickEvent(slot, 1, SlotActionType.THROW));
	}

	public void dropStackLocked(Slot slot) {
		if (getSlotState(slot).areInteractionsLocked()) {
			return;
		}

		setSlotState(slot, SlotInteractionState.TEMP_LOCKED);
		InteractionManager.push(unlockAfter(clickEventFactory.create(slot, 1, SlotActionType.THROW), slot));
	}

	public void dropAllOfAKind(Slot referenceSlot) {
		ItemStack stack = referenceSlot.getStack();
		if (stack.isEmpty()) {
			return;
		}

		ItemStack referenceStack = stack.copy();
		runInScope(getScope(referenceSlot), slot -> {
			if (ItemStackUtils.areItemsOfSameKind(slot.getStack(), referenceStack)) {
				dropStack(slot);
			}
		});
	}

	public void dropAllFrom(Slot referenceSlot) {
		runInScope(getScope(referenceSlot, true), true, this::dropStack);
	}
}
