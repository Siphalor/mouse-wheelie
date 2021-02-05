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

package de.siphalor.mousewheelie.client.inventory.sort;

import de.siphalor.tweed.tailor.DropdownMaterial;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import java.util.*;

public abstract class SortMode implements DropdownMaterial<SortMode> {
	private static final Map<String, SortMode> SORT_MODES = new HashMap<>();
	private final String name;

	public static final SortMode NONE, ALPHABET, QUANTITY, RAW_ID;

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

	public abstract Integer[] sort(Integer[] sortIds, ItemStack[] stacks);

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

	static {
		NONE = register("none", new SortMode("none") {
			@Override
			public Integer[] sort(Integer[] sortIds, ItemStack[] stacks) {
				return sortIds;
			}
		});
		ALPHABET = register("alphabet", new SortMode("alphabet") {
			String[] strings;
			ItemStack[] stacks;

			@Override
			public Integer[] sort(Integer[] sortIds, ItemStack[] stacks) {
				this.stacks = stacks;
				strings = Arrays.stream(sortIds).map(id -> {
					ItemStack itemStack = stacks[id];
					if (itemStack.isEmpty()) return "";
					return I18n.translate(itemStack.getName().getString());
				}).toArray(String[]::new);

				Arrays.sort(sortIds, (a, b) -> {
					if (strings[a].equals("")) {
						if (strings[b].equals(""))
							return 0;
						return 1;
					}
					if (strings[b].equals("")) return -1;
					int comp = strings[a].compareToIgnoreCase(strings[b]);
					if (comp == 0) {
						return Integer.compare(stacks[b].getCount(), stacks[a].getCount());
					}
					return comp;
				});

				return sortIds;
			}
		});
		QUANTITY = register("quantity", new SortMode("quantity") {
			@Override
			public Integer[] sort(Integer[] sortIds, ItemStack[] stacks) {
				HashMap<Item, Integer> itemToAmountMap = new HashMap<>();

				for (ItemStack stack : stacks) {
					if (stack.isEmpty()) continue;
					if (!itemToAmountMap.containsKey(stack.getItem())) {
						itemToAmountMap.put(stack.getItem(), stack.getCount());
					} else {
						itemToAmountMap.put(stack.getItem(), itemToAmountMap.get(stack.getItem()) + stack.getCount());
					}
				}

				Arrays.sort(sortIds, (a, b) -> {
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
					return Integer.compare(amountA, amountB);
				});

				return sortIds;
			}
		});
		RAW_ID = register("raw_id", new SortMode("raw_id") {
			@Override
			public Integer[] sort(Integer[] sortIds, ItemStack[] stacks) {
				Integer[] rawIds = Arrays.stream(stacks).map(stack -> stack.isEmpty() ? Integer.MAX_VALUE : Registry.ITEM.getId(stack.getItem())).toArray(Integer[]::new);

				Arrays.sort(sortIds, (a, b) -> {
					int result = Integer.compare(rawIds[a], rawIds[b]);
					if (result == 0) {
						if (stacks[b].isDamageable()) {
							return Integer.compare(stacks[a].getDamage(), stacks[b].getDamage());
						} else {
							return Integer.compare(stacks[b].getCount(), stacks[a].getCount());
						}
					}
					return result;
				});

				return sortIds;
			}
		});
	}
}
