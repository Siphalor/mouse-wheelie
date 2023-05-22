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

package de.siphalor.mousewheelie.client.inventory.sort;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.mousewheelie.client.util.StackMatcher;
import de.siphalor.tweed4.tailor.DropdownMaterial;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.*;
import java.util.concurrent.locks.Lock;

public abstract class SortMode implements DropdownMaterial<SortMode> {
	private static final Map<String, SortMode> SORT_MODES = new HashMap<>();
	private final String name;

	public static final SortMode NONE, ALPHABET, CREATIVE, QUANTITY, RAW_ID;

	public static <T extends SortMode> T register(String name, T sortMode) {
		SORT_MODES.put(name, sortMode);
		return sortMode;
	}

	public static void unregister(String name) {
		SORT_MODES.remove(name);
	}

	protected SortMode(String name) {
		this.name = name;
	}

	/**
	 * Sorts the given slot ids using the given stacks in the slots. Sorting may be done in place.
	 * @param sortIds An array of the current slot indices
	 * @param stacks The stacks in the respective slots
	 * @param context Additional context for the sorting
	 * @return The sorted array of slot indices
	 */
	public int[] sort(int[] sortIds, ItemStack[] stacks, SortContext context) {
		Integer[] boxedSortIds = new Integer[sortIds.length];
		for (int i = 0; i < sortIds.length; i++) {
			boxedSortIds[i] = sortIds[i];
		}
		boxedSortIds = sort(boxedSortIds, stacks);
		for (int i = 0; i < sortIds.length; i++) {
			sortIds[i] = boxedSortIds[i];
		}
		return sortIds;
	}

	/**
	 * Sorts the given slot ids using the given stacks in the slots. Sorting may be done in place.
	 * @param sortIds An array of the current slot indices
	 * @param stacks The stacks in the respective slots
	 * @return The sorted array of slot indices
	 * @deprecated Please use the primitive variant {@link SortMode#sort(int[], ItemStack[], SortContext)} instead.
	 * @see SortMode#sort(int[], ItemStack[], SortContext)
	 */
	@Deprecated
	public Integer[] sort(Integer[] sortIds, ItemStack[] stacks) {
		return sortIds;
	}

	@Override
	public DropdownMaterial<SortMode> valueOf(String s) {
		return SORT_MODES.get(s);
	}

	@Override
	public Collection<SortMode> values() {
		return SORT_MODES.values();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String getTranslationKey() {
		return "mousewheelie.sortmode." + name.toLowerCase(Locale.ENGLISH);
	}

	private static void sortByValues(int[] sortIds, ItemStack[] stacks, int[] values) {
		IntArrays.quickSort(sortIds, (a, b) -> {
			int cmp = Integer.compare(values[a], values[b]);
			if (cmp != 0) {
				return cmp;
			}
			return ItemStackUtils.compareEqualItems(stacks[a], stacks[b]);
		});
	}

	static {
		NONE = register("none", new SortMode("none") {});
		ALPHABET = register("alphabet", new SortMode("alphabet") {
			@Override
			public int[] sort(int[] sortIds, ItemStack[] stacks, SortContext context) {
				String[] strings = new String[sortIds.length];
				for (int i = 0; i < sortIds.length; i++) {
					ItemStack stack = stacks[i];
					strings[i] = stack.isEmpty() ? "" : stack.getName().getString();
				}

				IntArrays.quickSort(sortIds, (a, b) -> {
					if (strings[a].equals("")) {
						if (strings[b].equals(""))
							return 0;
						return 1;
					}
					if (strings[b].equals("")) return -1;
					int comp = strings[a].compareToIgnoreCase(strings[b]);
					if (comp == 0) {
						return ItemStackUtils.compareEqualItems(stacks[a], stacks[b]);
					}
					return comp;
				});

				return sortIds;
			}
		});
		CREATIVE = register("creative", new SortMode("creative") {
			@Override
			public int[] sort(int[] sortIds, ItemStack[] stacks, SortContext context) {
				int[] sortValues = new int[sortIds.length];
				if (MWConfig.sort.optimizeCreativeSearchSort) {
					Lock lock = CreativeSearchOrder.getReadLock();
					lock.lock();
					for (int i = 0; i < stacks.length; i++) {
						sortValues[i] = CreativeSearchOrder.getStackSearchPosition(stacks[i]);
					}
					lock.unlock();
				} else {
					Collection<ItemStack> displayStacks = ItemGroups.getSearchGroup().getDisplayStacks();
					List<ItemStack> displayStackList;
					if (displayStacks instanceof List) {
						displayStackList = (List<ItemStack>) displayStacks;
					} else {
						displayStackList = new ArrayList<>(displayStacks);
					}
					Object2IntMap<StackMatcher> lookup = new Object2IntOpenHashMap<>(stacks.length);
					for (int i = 0; i < stacks.length; i++) {
						final ItemStack stack = stacks[i];
						sortValues[i] = lookup.computeIfAbsent(StackMatcher.of(stack), matcher -> {
							int index = displayStackList.indexOf(matcher);
							if (index == -1) {
								return lookup.computeIfAbsent(StackMatcher.ignoreNbt(stack), matcher2 -> {
									int plainIndex = displayStackList.indexOf(matcher2);
									if (plainIndex == -1) {
										return Integer.MAX_VALUE;
									}
									return plainIndex;
								});
							}
							return index;
						});
					}
				}
				SortMode.sortByValues(sortIds, stacks, sortValues);
				return sortIds;
			}
		});
		QUANTITY = register("quantity", new SortMode("quantity") {
			@Override
			public int[] sort(int[] sortIds, ItemStack[] stacks, SortContext context) {
				HashMap<Item, Integer> itemToAmountMap = new HashMap<>();

				for (ItemStack stack : stacks) {
					if (stack.isEmpty()) continue;
					if (!itemToAmountMap.containsKey(stack.getItem())) {
						itemToAmountMap.put(stack.getItem(), stack.getCount());
					} else {
						itemToAmountMap.put(stack.getItem(), itemToAmountMap.get(stack.getItem()) + stack.getCount());
					}
				}

				IntArrays.quickSort(sortIds, (a, b) -> {
					ItemStack stack = stacks[a];
					ItemStack stack2 = stacks[b];
					if (stack.isEmpty()) {
						return stack2.isEmpty() ? 0 : 1;
					}
					if (stack2.isEmpty()) {
						return -1;
					}
					Integer amountA = itemToAmountMap.get(stack.getItem());
					Integer amountB = itemToAmountMap.get(stack2.getItem());
					int cmp = Integer.compare(amountB, amountA);
					if (cmp != 0) {
						return cmp;
					}
					return ItemStackUtils.compareEqualItems(stack, stack2);
				});

				return sortIds;
			}
		});
		RAW_ID = register("raw_id", new SortMode("raw_id") {
			@Override
			public int[] sort(int[] sortIds, ItemStack[] stacks, SortContext context) {
				int[] rawIds = Arrays.stream(stacks).mapToInt(stack -> stack.isEmpty() ? Integer.MAX_VALUE : Registries.ITEM.getRawId(stack.getItem())).toArray();
				sortByValues(sortIds, stacks, rawIds);
				return sortIds;
			}
		});
	}
}
