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
import de.siphalor.mousewheelie.client.network.InteractionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.DefaultedList;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SlotRefiller {
	private static PlayerInventory playerInventory;
	private static ItemStack stack;

	private static final ConcurrentLinkedDeque<Rule> rules = new ConcurrentLinkedDeque<>();

	public static void set(PlayerInventory playerInventory, ItemStack stack) {
		SlotRefiller.playerInventory = playerInventory;
		SlotRefiller.stack = stack;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean refill() {
		Iterator<Rule> iterator = rules.descendingIterator();
		while (iterator.hasNext()) {
			Rule rule = iterator.next();
			if (rule.matches(stack)) {
				int slot = rule.findMatchingStack(playerInventory, stack);
				if (slot != -1) {
					if (slot == playerInventory.selectedSlot) {
						return true;
					}
					if (slot < 9) {
						playerInventory.selectedSlot = slot;
						InteractionManager.push(new InteractionManager.PacketEvent(new UpdateSelectedSlotC2SPacket(slot)));
					} else {
						InteractionManager.push(new InteractionManager.PacketEvent(new PickFromInventoryC2SPacket(slot), triggerType -> triggerType == InteractionManager.TriggerType.HELD_ITEM_CHANGE));
					}
					return true;
				}
			}
		}
		return false;
	}

	static {
		rules.add(new BlockRule());
		rules.add(new ItemGroupRule());
		rules.add(new ItemHierarchyRule());
		rules.add(new BlockHierarchyRule());
		rules.add(new FoodRule());
		rules.add(new EqualItemRule());
		rules.add(new EqualStackRule());
	}

	public static abstract class Rule {
		public Rule() {
			rules.add(this);
		}

		abstract boolean matches(ItemStack oldStack);

		abstract int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack);

		protected int iterateInventory(PlayerInventory playerInventory, Function<ItemStack, Boolean> consumer) {
			for (int i = 0; i < playerInventory.main.size(); i++) {
				if (consumer.apply(playerInventory.main.get(i)))
					return i;
			}
			return -1;
		}
	}

	public static class BlockRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MWConfig.refill.rules.anyBlock && oldStack.getItem() instanceof BlockItem;
		}

		@Override
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
			return iterateInventory(playerInventory, stack -> stack.getItem() instanceof BlockItem);
		}
	}

	public static class ItemGroupRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MWConfig.refill.rules.itemgroup && oldStack.getItem().getGroup() != null;
		}

		@Override
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
			ItemGroup group = oldStack.getItem().getGroup();
			return iterateInventory(playerInventory, stack -> stack.getItem().getGroup() == group);
		}
	}

	public static class ItemHierarchyRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MWConfig.refill.rules.itemHierarchy && oldStack.getItem().getClass() != Item.class && !(oldStack.getItem() instanceof BlockItem);
		}

		@Override
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
			int currentRank = 0;
			ConcurrentLinkedQueue<Class<?>> classes = new ConcurrentLinkedQueue<>();
			Class<?> clazz = oldStack.getItem().getClass();
			while (clazz != Item.class) {
				classes.add(clazz);
				clazz = clazz.getSuperclass();
			}
			int classesSize = classes.size();
			if (classesSize == 0)
				return -1;

			int index = -1;

			DefaultedList<ItemStack> mainInv = playerInventory.main;
			outer:
			for (int i = 0; i < mainInv.size(); i++) {
				clazz = mainInv.get(i).getItem().getClass();
				while (clazz != Item.class) {
					int classRank = classesSize;
					for (Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); classRank--) {
						if (classRank <= 0) break;
						if (classRank <= currentRank) continue outer;
						if (clazz.equals(iterator.next())) {
							if (classRank >= classesSize) return i;
							currentRank = classRank;
							index = i;
							continue outer;
						}
					}
					clazz = clazz.getSuperclass();
				}
			}
			return index;
		}
	}

	public static class BlockHierarchyRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MWConfig.refill.rules.blockHierarchy && oldStack.getItem() instanceof BlockItem;
		}

		@Override
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
			int currentRank = 0;
			ConcurrentLinkedQueue<Class<?>> classes = new ConcurrentLinkedQueue<>();
			Class<?> clazz = ((BlockItem) oldStack.getItem()).getBlock().getClass();
			while (clazz != Block.class) {
				classes.add(clazz);
				clazz = clazz.getSuperclass();
			}
			int classesSize = classes.size();
			if (classesSize == 0)
				return -1;

			int index = -1;
			DefaultedList<ItemStack> mainInv = playerInventory.main;

			outer:
			for (int i = 0; i < mainInv.size(); i++) {
				if (!(mainInv.get(i).getItem() instanceof BlockItem)) continue;
				clazz = ((BlockItem) mainInv.get(i).getItem()).getBlock().getClass();
				while (clazz != Block.class) {
					int classRank = classesSize;
					for (Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); classRank--) {
						if (classRank <= 0) break;
						if (classRank <= currentRank) continue outer;
						if (clazz.equals(iterator.next())) {
							if (classRank >= classesSize) return i;
							currentRank = classRank;
							index = i;
							continue outer;
						}
					}
					clazz = clazz.getSuperclass();
				}
			}
			return index;
		}
	}

	public static class FoodRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MWConfig.refill.rules.food && oldStack.isFood();
		}

		@Override
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
			return iterateInventory(playerInventory, ItemStack::isFood);
		}
	}

	public static class EqualItemRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MWConfig.refill.rules.equalItems;
		}

		@Override
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
			Item item = oldStack.getItem();
			return iterateInventory(playerInventory, stack -> stack.getItem() == item);
		}
	}

	public static class EqualStackRule extends Rule {
		@Override
		boolean matches(ItemStack oldStack) {
			return MWConfig.refill.rules.equalStacks;
		}

		@Override
		int findMatchingStack(PlayerInventory playerInventory, ItemStack oldStack) {
			return playerInventory.method_7371(oldStack);
		}
	}
}
