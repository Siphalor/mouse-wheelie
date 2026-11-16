/*
 * Copyright 2020 Siphalor and contributors
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

//- import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.view.InventoryView;
import de.siphalor.mousewheelie.client.inventory.view.InventoryViewEntry;
import de.siphalor.mousewheelie.client.inventory.view.InventoryViewLocation;
import de.siphalor.mousewheelie.client.network.InteractionManager;
//- import de.siphalor.mousewheelie.client.network.MWClientNetworking;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
//- import net.minecraft.world.item.enchantment.EnchantmentHelper;
//- import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SlotRefiller {
	/**
	 * Indicates the maximum time in milliseconds a refill is expected to take.
	 * If a refill has been started with no recorded end, it is treated as done after this time.
	 */
	private static final long MAX_REFILL_MILLIS = 5000;
	private static final InteractionManager.InteractionEvent REFILL_END_EVENT = () -> {
		endRefill();
		return InteractionManager.DUMMY_WAITER;
	};

	private static Inventory playerInventory;
	private static InventoryView inventoryView;
	private static ItemStack stack;
	private static long refillStartTime = System.currentTimeMillis() - MAX_REFILL_MILLIS;

	private static final ConcurrentLinkedDeque<Rule> rules = new ConcurrentLinkedDeque<>();
	private static InteractionHand refillHand = null;

	private SlotRefiller() {}

	/**
	 * Schedules a refill if a refill scenario is encountered.
	 * @param hand the hand to potentially refill
	 * @param inventory the player inventory
	 * @param oldStack the old stack in the hand
	 * @param newStack the new stack in the hand
	 * @return whether a refill has been scheduled
	 */
	public static boolean scheduleRefillChecked(InteractionHand hand, Inventory inventory, ItemStack oldStack, ItemStack newStack) {
		if (MWClient.getOpenScreen() != null) {
			return false;
		}

		if (!oldStack.isEmpty() && (newStack.isEmpty() || (MouseWheelie.config.refill.itemChanges && oldStack.getItem() != newStack.getItem()))) {
			scheduleRefillUnchecked(hand, inventory, oldStack.copy());
			return true;
		}
		return false;
	}

	/**
	 * Unconditionally schedules a refill.
	 * @param hand the hand to refill
	 * @param inventory the player inventory
	 * @param referenceStack the stack to decide the refilling by
	 */
	public static void scheduleRefillUnchecked(InteractionHand hand, Inventory inventory, ItemStack referenceStack) {
		refillHand = hand;
		setupRefill(inventory, referenceStack);
	}

	public static void performRefill() {
		if (refillHand == null) return;

		if (refillHand == InteractionHand.OFF_HAND && !MouseWheelie.config.refill.offHand) {
			clearRefill();
			return;
		}

		refill(refillHand);
		clearRefill();
	}

	public static void clearRefill() {
		refillHand = null;
	}

	public static void setupRefill(Inventory playerInventory, ItemStack stack) {
		SlotRefiller.playerInventory = playerInventory;
		SlotRefiller.inventoryView = InventoryView.ofContainerRange(playerInventory, 0, Inventory.INVENTORY_SIZE);
		//# if MC_VERSION_NUMBER >= 12103
		if (MouseWheelie.config.refill.fromBundles) {
			SlotRefiller.inventoryView = InventoryView.appendingBundles(SlotRefiller.inventoryView);
		}
		//# end
		SlotRefiller.stack = stack;
	}

	@SuppressWarnings("UnusedReturnValue")
	public static void refill(InteractionHand hand) {
		if (isRefillInProgress()) {
			return;
		}
		if (MouseWheelie.config.refill.ignoreBuckets && stack.getItem() instanceof BucketItem) {
			return;
		}
		//# if MC_VERSION_NUMBER >= 12101
		if (!stack.getOrDefault(
				EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION,
				Collections.emptyList()
		).isEmpty()) {
			return;
		}
		//# else
		//- if (stack.getItem() == Items.TRIDENT && EnchantmentHelper.getLoyalty(stack) > 0) {
		//- 	return;
		//- }
		//# end

		Iterator<Rule> iterator = rules.descendingIterator();
		while (iterator.hasNext()) {
			Rule rule = iterator.next();
			if (!rule.matches(stack)) {
				continue;
			}

			Optional<InventoryViewLocation> location = rule.findMatchingStack(inventoryView, stack);

			if (location.isPresent()) {
				refillFromLocation(hand, location.get());
				return;
			}
		}
	}

	private static void startRefill() {
		refillStartTime = System.currentTimeMillis();
	}

	private static void endRefill() {
		refillStartTime = System.currentTimeMillis() - MAX_REFILL_MILLIS;
	}

	private static boolean isRefillInProgress() {
		return System.currentTimeMillis() - refillStartTime < MAX_REFILL_MILLIS;
	}

	private static void scheduleRefillSound() {
		if (MouseWheelie.config.refill.playSound) {
			InteractionManager.delay(SlotRefiller::playRefillSound, Duration.of(200, ChronoUnit.MILLIS));
		}
	}

	private static void playRefillSound() {
		//# if MC_VERSION_NUMBER >= 12102
		Minecraft.getInstance().schedule(() ->
		//# else
		//- Minecraft.getInstance().tell(() ->
		//# end
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 0.2F, 1F))
		);
	}

	private static void refillFromLocation(InteractionHand hand, InventoryViewLocation location) {
		//# if MC_VERSION_NUMBER >= 12108
		if (location.getInventorySlotId() == playerInventory.getSelectedSlot()) {
		//# else
		//- if (location.getInventorySlotId() == playerInventory.selected) {
		//# end
			return;
		}

		startRefill();

		StackPicker.TargetMode targetMode;
		if (hand == InteractionHand.OFF_HAND) {
			targetMode = StackPicker.TargetMode.OFFHAND;
		} else if (MouseWheelie.config.refill.alwaysKeepSelectedSlot) {
			targetMode = StackPicker.TargetMode.KEEP_SELECTED_HOTBAR_SLOT;
		} else {
			targetMode = StackPicker.TargetMode.PREFER_EMPTY_HOTBAR_SLOTS;
		}

		StackPicker stackPicker = new StackPicker(playerInventory.player);
		if (stackPicker.pick(location, new StackPicker.Options(targetMode, false))) {
			scheduleRefillSound();
		}

		InteractionManager.push(REFILL_END_EVENT);
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

	public abstract static class Rule {
		/**
		 * Creates a new rule.
		 * Automatically registers this rule to the list of rules.
		 */
		protected Rule() {
			rules.add(this);
		}

		/**
		 * Checks if the rule is valid for the given stack.
		 *
		 * @param oldStack The stack to check.
		 * @return Whether the rule applies to the given stack.
		 */
		public abstract boolean matches(ItemStack oldStack);

		/**
		 * Find a matching slot for the given base stack in the player inventory.
		 *
		 * @param inventoryView the inventory to search in.
		 * @param oldStack      The base stack to search for.
		 * @return The inventory entry, if any matching slot was found.
		 */
		public abstract Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack);
	}

	public static class BlockRule extends Rule {
		@Override
		public boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.anyBlock && oldStack.getItem() instanceof BlockItem;
		}

		@Override
		public Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack) {
			for (InventoryViewEntry entry : inventoryView) {
				if (entry.getStack().getItem() instanceof BlockItem) {
					return Optional.of(entry.getLocation());
				}
			}
			return Optional.empty();
		}
	}

	public static class ItemGroupRule extends Rule {

		private static boolean containsBroad(CreativeModeTab group, ItemStack stack) {
			return group.contains(stack) || group.contains(stack.getItem().getDefaultInstance());
		}

		@Override
		public boolean matches(ItemStack oldStack) {
			if (!MouseWheelie.config.refill.rules.itemgroup) {
				return false;
			}
			for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
				if (containsBroad(group, oldStack)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack) {
			List<CreativeModeTab> checkGroups = new ArrayList<>();
			for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
				if (containsBroad(group, oldStack)) {
					checkGroups.add(group);
				}
			}
			if (checkGroups.isEmpty()) {
				return Optional.empty();
			}

			for (InventoryViewEntry entry : inventoryView) {
				for (CreativeModeTab group : checkGroups) {
					if (containsBroad(group, entry.getStack())) {
						return Optional.of(entry.getLocation());
					}
				}
			}
			return Optional.empty();
		}
	}

	public static class ItemHierarchyRule extends Rule {
		@Override
		public boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.itemHierarchy
					&& oldStack.getItem().getClass() != Item.class && !(oldStack.getItem() instanceof BlockItem);
		}

		@Override
		public Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack) {
			return findBestThroughClassHierarchy(oldStack, inventoryView, Item::getClass, Item.class);
		}
	}

	public static class BlockHierarchyRule extends Rule {
		@Override
		public boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.blockHierarchy && oldStack.getItem() instanceof BlockItem;
		}

		@Override
		public Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack) {
			return findBestThroughClassHierarchy(oldStack, inventoryView, item -> {
				if (item instanceof BlockItem) {
					return ((BlockItem) item).getBlock().getClass();
				} else {
					return null;
				}
			}, Block.class);
		}
	}

	public static Optional<InventoryViewLocation> findBestThroughClassHierarchy(
			ItemStack baseStack,
			InventoryView inventoryView,
			Function<Item, Class<?>> getClass,
			Class<?> baseClass
	) {
		Collection<Class<?>> classes = new ArrayList<>(10);
		Class<?> clazz = getClass.apply(baseStack.getItem());
		while (clazz != baseClass) {
			classes.add(clazz);
			clazz = clazz.getSuperclass();
		}
		int classesSize = classes.size();
		if (classesSize == 0)
			return Optional.empty();

		int bestRank = 0;
		InventoryViewLocation bestLocation = null;

		outer:
		for (InventoryViewEntry entry : inventoryView) {
			clazz = getClass.apply(entry.getStack().getItem());
			if (clazz == null) {
				continue;
			}
			while (clazz != baseClass) {
				int classRank = classesSize;
				for (Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); classRank--) {
					if (classRank <= 0) break;
					if (classRank <= bestRank) continue outer;
					if (Objects.equals(clazz, iterator.next())) {
						if (classRank >= classesSize) {
							break outer;
						}
						bestRank = classRank;
						bestLocation = entry.getLocation();
						continue outer;
					}
				}
				clazz = clazz.getSuperclass();
			}
		}

		return Optional.ofNullable(bestLocation);
	}

	public static class FoodRule extends Rule {
		@Override
		public boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.food && isEdible(oldStack);
		}

		@Override
		public Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack) {
			for (InventoryViewEntry entry : inventoryView) {
				if (isEdible(entry.getStack())) {
					return Optional.of(entry.getLocation());
				}
			}
			return Optional.empty();
		}

		private static boolean isEdible(ItemStack stack) {
			//# if MC_VERSION_NUMBER >= 12006
			return stack.has(DataComponents.FOOD);
			//# else
			//- return stack.isEdible();
			//# end
		}
	}

	public static class EqualItemRule extends Rule {
		@Override
		public boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.equalItems;
		}

		@Override
		public Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack) {
			Item item = oldStack.getItem();
			for (InventoryViewEntry entry : inventoryView) {
				if (entry.getStack().getItem() == item) {
					return Optional.of(entry.getLocation());
				}
			}
			return Optional.empty();
		}
	}

	public static class EqualStackRule extends Rule {
		@Override
		public boolean matches(ItemStack oldStack) {
			return MouseWheelie.config.refill.rules.equalStacks;
		}

		@Override
		public Optional<InventoryViewLocation> findMatchingStack(InventoryView inventoryView, ItemStack oldStack) {
			for (InventoryViewEntry entry : inventoryView) {
				if (ItemStackUtils.canCombine(entry.getStack(), oldStack)) {
					return Optional.of(entry.getLocation());
				}
			}
			return Optional.empty();
		}
	}
}
