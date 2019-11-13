package de.siphalor.mousewheelie.client.inventory.sort;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public abstract class SortMode implements Comparator<Integer> {
	public static final SortMode ALPHABET, QUANTITY, RAW_ID;

	void init(Integer[] sortIds, ItemStack[] stacks) {};

	public enum Predefined {
		ALPHABET(SortMode.ALPHABET), QUANTITY(SortMode.QUANTITY), RAW_ID(SortMode.RAW_ID), NONE(null);
		public SortMode sortMode;

		Predefined(SortMode sortMode) {
			this.sortMode = sortMode;
		}
	}

	static {
		ALPHABET = new SortMode() {
			String[] strings;
			ItemStack[] stacks;
			@Override
			public void init(Integer[] sortIds, ItemStack[] stacks) {
				this.stacks = stacks;
				strings = Arrays.stream(sortIds).map(id -> {
					ItemStack itemStack = stacks[id];
					if (itemStack.isEmpty()) return "";
					return I18n.translate(itemStack.getTranslationKey());
				}).toArray(String[]::new);
			}

			@Override
			public int compare(Integer o1, Integer o2) {
				if (strings[o1].equals("")) {
						if (strings[o2].equals(""))
							return 0;
						return 1;
					}
					if (strings[o2].equals("")) return -1;
					int comp = strings[o1].compareToIgnoreCase(strings[o2]);
					if (comp == 0) {
						return Integer.compare(stacks[o2].getCount(), stacks[o1].getCount());
					}
					return comp;
			}
		};
		QUANTITY = new SortMode() {
			HashMap<Item, HashMap<CompoundTag, Integer>> itemToAmountMap = new HashMap<>();
			ItemStack[] stacks;
			@Override
			void init(Integer[] sortIds, ItemStack[] stacks) {
				this.stacks = stacks;
				for(ItemStack stack : stacks) {
					if (stack.isEmpty()) continue;
					if (!itemToAmountMap.containsKey(stack.getItem())) {
						HashMap<CompoundTag, Integer> newMap = new HashMap<>();
						newMap.put(stack.getOrCreateTag(), stack.getCount());
						itemToAmountMap.put(stack.getItem(), newMap);
					} else {
						HashMap<CompoundTag, Integer> itemMap = itemToAmountMap.get(stack.getItem());
						if (!itemMap.containsKey(stack.getOrCreateTag())) {
							itemMap.put(stack.getTag(), stack.getCount());
						} else {
							itemMap.replace(stack.getTag(), itemMap.get(stack.getTag()) + stack.getCount());
						}
					}
				}
			}

			@Override
			public int compare(Integer o1, Integer o2) {
				ItemStack stack = stacks[o1];
				ItemStack stack2 = stacks[o2];
				if (stack.isEmpty()) {
					return stack2.isEmpty() ? 0 : 1;
				}
				if (stack2.isEmpty()) {
					return -1;
				}
				Integer a = itemToAmountMap.get(stack.getItem()).get(stack.getTag());
				Integer a2 = itemToAmountMap.get(stack2.getItem()).get(stack2.getTag());
				return Integer.compare(a2, a);
			}
		};
		RAW_ID = new SortMode() {
			Integer[] rawIds;
			ItemStack[] stacks;
			@Override
			void init(Integer[] sortIds, ItemStack[] stacks) {
				this.stacks = stacks;
				rawIds = Arrays.stream(stacks).map(stack -> stack.isEmpty() ? Integer.MAX_VALUE : Registry.ITEM.getRawId(stack.getItem())).toArray(Integer[]::new);
			}

			@Override
			public int compare(Integer o1, Integer o2) {
				int result = Integer.compare(rawIds[o1], rawIds[o2]);
				if(result == 0) return Integer.compare(stacks[o2].getCount(), stacks[o1].getCount());
				return result;
			}
		};
	}
}
