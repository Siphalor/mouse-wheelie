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

package de.siphalor.mousewheelie.client.util;

import com.google.common.collect.Sets;
import de.siphalor.mousewheelie.MouseWheelie;
//- import de.siphalor.mousewheelie.client.mixin.MinecraftClientAccessor;
import java.awt.*;
import java.util.*;
import lombok.CustomLog;
import org.apache.commons.lang3.builder.HashCodeBuilder;

//- import net.minecraft.client.Minecraft;
//- import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
//- import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
//- import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;

@CustomLog
public class ItemStackUtils {
	//# if MC_VERSION_NUMBER >= 12104
	private static final Item.TooltipContext TOOLTIP_CONTEXT = Item.TooltipContext.EMPTY;
	//# elif MC_VERSION_NUMBER >= 12006
	//- private static final Item.TooltipContext TOOLTIP_CONTEXT = Item.TooltipContext.EMPTY;
	//- private static final ItemColors ITEM_COLORS = ((MinecraftClientAccessor) Minecraft.getInstance()).getItemColors();
	//# else
	//- private static final CompoundTag EMPTY_COMPOUND = new CompoundTag();
	//# end

	public static boolean hasCustomData(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12006
		return !stack.getComponentsPatch().isEmpty();
		//# else
		//- return stack.hasTag();
		//# end
	}

	public static int getMaxStackSize(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12006
		return stack.getMaxStackSize();
		//# else
		//- return stack.getItem().getMaxStackSize();
		//# end
	}

	public static boolean canCombine(ItemStack a, ItemStack b) {
		//# if MC_VERSION_NUMBER >= 12006
		return ItemStack.isSameItemSameComponents(a, b);
		//# else
		//- return ItemStack.isSameItemSameTags(a, b);
		//# end
	}

	public static int compareEqualItems(ItemStack a, ItemStack b) {
		// compare counts
		int cmp = Integer.compare(b.getCount(), a.getCount());
		if (cmp != 0) {
			return cmp;
		}
		return compareEqualItems2(a, b);
	}

	private static int compareEqualItems2(ItemStack a, ItemStack b) {
		// compare names
		if (hasCustomName(a)) {
			if (!hasCustomName(b)) {
				return -1;
			}
			return compareEqualItems3(a, b);
		}
		if (hasCustomName(b)) {
			return 1;
		}
		return compareEqualItems3(a, b);
	}

	private static boolean hasCustomName(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12006
		return stack.has(DataComponents.CUSTOM_NAME);
		//# else
		//- return stack.hasCustomHoverName();
		//# end
	}

	private static int compareEqualItems3(ItemStack a, ItemStack b) {
		// compare tooltips
		Iterator<Component> tooltipsA = getTooltipLines(a);
		Iterator<Component> tooltipsB = getTooltipLines(b);

		while (tooltipsA.hasNext()) {
			if (!tooltipsB.hasNext()) {
				return 1;
			}

			int cmp = tooltipsA.next().getString().compareToIgnoreCase(tooltipsB.next().getString());
			if (cmp != 0) {
				return cmp;
			}
		}
		if (tooltipsB.hasNext()) {
			return -1;
		}
		return compareEqualItems4(a, b);
	}

	private static Iterator<Component> getTooltipLines(ItemStack stack) {
		try {
			//# if MC_VERSION_NUMBER >= 12006
			return stack.getTooltipLines(TOOLTIP_CONTEXT, null, TooltipFlag.Default.NORMAL).iterator();
			//# else
			//- return stack.getTooltipLines(null, TooltipFlag.Default.NORMAL).iterator();
			//# end
		} catch (Exception e) {
			log.debug("Uncaught exception while resolving tooltip of stack {}", stack, e);
			return Collections.emptyIterator();
		}
	}

	private static int compareEqualItems4(ItemStack a, ItemStack b) {
		// compare color
		//# if MC_VERSION_NUMBER >= 12104
		int colorA = DyedItemColor.getOrDefault(a, 0);
		int colorB = DyedItemColor.getOrDefault(b, 0);
		if (colorA == 0 && colorB != 0) {
			return -1;
		} else if (colorA != 0 && colorB == 0) {
			return 1;
		} else if (colorA != 0) {
		//# elif MC_VERSION_NUMBER >= 12006
		//- int colorA = ITEM_COLORS.getColor(a, 0);
		//- int colorB = ITEM_COLORS.getColor(b, 0);
		//- if (colorA == -1 && colorB != -1) {
		//- 	return -1;
		//- } else if (colorA != -1 && colorB == -1) {
		//- 	return 1;
		//- } else if (colorA != -1) {
		//# else
		//- Item item = a.getItem();
		//- if (item instanceof DyeableLeatherItem) {
		//- 	int colorA = ((DyeableLeatherItem) item).getColor(a);
		//- 	int colorB = ((DyeableLeatherItem) item).getColor(b);
		//# end
			float[] hsbA = Color.RGBtoHSB(colorA >> 16 & 0xFF, colorA >> 8 & 0xFF, colorA & 0xFF, null);
			float[] hsbB = Color.RGBtoHSB(colorB >> 16 & 0xFF, colorB >> 8 & 0xFF, colorB & 0xFF, null);
			int cmp = Float.compare(hsbA[0], hsbB[0]);
			if (cmp != 0) {
				return cmp;
			}
			cmp = Float.compare(hsbA[1], hsbB[1]);
			if (cmp != 0) {
				return cmp;
			}
			cmp = Float.compare(hsbA[2], hsbB[2]);
			if (cmp != 0) {
				return cmp;
			}
		}
		return compareEqualItems5(a, b);
	}

	private static int compareEqualItems5(ItemStack a, ItemStack b) {
		// compare damage
		return Integer.compare(a.getDamageValue(), b.getDamageValue());
	}

	public static boolean areItemsOfSameKind(ItemStack stack1, ItemStack stack2) {
		return areItemsOfSameKind(stack1, stack2, MouseWheelie.config.general.itemKindsNbtMatchMode);
	}

	public static boolean areItemsOfSameKind(ItemStack stack1, ItemStack stack2, NbtMatchMode mode) {
		switch (mode) {
			case NONE -> {
				return stack1.getItem() == stack2.getItem();
			}
			case ALL -> {
				return canCombine(stack1, stack2);
			}
			case SOME -> {
				if (!ItemStack.isSameItem(stack1, stack2)) {
					return false;
				}
				//# if MC_VERSION_NUMBER >= 12006
				return areComponentsEqualExcept(stack1, stack2, DataComponents.DAMAGE, DataComponents.ENCHANTMENTS);
				//# else
				//- return areTagsEqualExcept(stack1, stack2, "Damage", "Enchantments");
				//# end
			}
		}
		return false; // unreachable
	}

	//# if MC_VERSION_NUMBER >= 12006
	private static boolean areComponentsEqualExcept(ItemStack a, ItemStack b, DataComponentType<?>... keys) {
		DataComponentMap componentsA = a.getComponents();
		DataComponentMap componentsB = b.getComponents();
		Set<DataComponentType<?>> checkedKeys = Sets.newHashSet(keys);
		if (!areComponentsEqualExceptOneSided(componentsA, componentsB, checkedKeys)) {
			return false;
		}
		return areComponentsEqualExceptOneSided(componentsA, componentsB, checkedKeys);
	}

	private static boolean areComponentsEqualExceptOneSided(
			DataComponentMap componentsA, DataComponentMap componentsB, Set<DataComponentType<?>> checkedKeys
	) {
		for (TypedDataComponent<?> componentA : componentsA) {
			if (checkedKeys.contains(componentA.type())) {
				continue;
			}
			Object valueB = componentsB.get(componentA.type());
			if (!Objects.equals(componentA.value(), valueB)) {
				return false;
			}
			checkedKeys.add(componentA.type());
		}
		return true;
	}
	//# else
	//- private static boolean areTagsEqualExcept(ItemStack a, ItemStack b, String... keys) {
	//- 	CompoundTag tagA = getTagOrEmpty(a);
	//- 	CompoundTag tagB = getTagOrEmpty(b);
	//- 	Set<String> checkedKeys = Sets.newHashSet(keys);
	//- 	if (!areTagsEqualExceptOneSided(tagA, tagB, checkedKeys)) {
	//- 		return false;
	//- 	}
	//- 	return areTagsEqualExceptOneSided(tagB, tagA, checkedKeys);
	//- }

	//- private static boolean areTagsEqualExceptOneSided(CompoundTag tagA, CompoundTag tagB, Set<String> checkedKeys) {
	//- 	for (String key : tagA.getAllKeys()) {
	//- 		if (checkedKeys.contains(key)) {
	//- 			continue;
	//- 		}
	//- 		if (!tagB.contains(key)) {
	//- 			return false;
	//- 		}
	//- 		//noinspection ConstantConditions
	//- 		if (!tagA.get(key).equals(tagB.get(key))) {
	//- 			return false;
	//- 		}
	//- 		checkedKeys.add(key);
	//- 	}
	//- 	return true;
	//- }

	//- private static CompoundTag getTagOrEmpty(ItemStack stack) {
	//- 	if (stack.hasTag()) {
	//- 		return stack.getTag();
	//- 	}
	//- 	return EMPTY_COMPOUND;
	//- }
	//# end

	public static int hashByKind(ItemStack stack, NbtMatchMode mode) {
		switch (mode) {
			case NONE:
				return stack.getItem().hashCode();
			case ALL:
				//# if MC_VERSION_NUMBER >= 12006
				return stack.getItem().hashCode() + stack.getComponentsPatch().hashCode();
				//# else
				//- return stack.hashCode();
				//# end
			case SOME:
				return hashStackWithSomeNbt(stack);
			default:
				return 0; // unreachable
		}
	}

	private static int hashStackWithSomeNbt(ItemStack stack) {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder()
				.append(stack.getItem());
		//# if MC_VERSION_NUMBER >= 260300
		net.minecraft.core.component.DataComponentPatch.SplitResult split = stack.getComponentsPatch().split();
		split.added().stream()
				.filter(component -> component.type() != DataComponents.DAMAGE && component.type() != DataComponents.ENCHANTMENTS)
				.sorted(Comparator.comparing(component -> component.type().hashCode()))
				.forEachOrdered(component -> hashCodeBuilder.append(component.type()).append(component.value()));
		split.removed().stream()
				.filter(type -> type != DataComponents.DAMAGE && type != DataComponents.ENCHANTMENTS)
				.sorted(Comparator.comparing(Object::hashCode))
				.forEachOrdered(hashCodeBuilder::append);
		//# elif MC_VERSION_NUMBER >= 12006
		//- stack.getComponentsPatch().entrySet().stream()
		//- 		.filter(entry -> entry.getKey() != DataComponents.DAMAGE && entry.getKey() != DataComponents.ENCHANTMENTS)
		//- 		.sorted(Comparator.comparing(entry -> entry.getKey().hashCode()))
		//- 		.forEachOrdered(entry -> hashCodeBuilder.append(entry.getKey()).append(entry.getValue()));
		//# else
		//- CompoundTag nbt = stack.getTag();
		//- if (nbt == null) {
		//- 	return hashCodeBuilder.toHashCode();
		//- }

		//- nbt.getAllKeys().stream().sorted().forEachOrdered(key -> {
		//- 	if (key.equals("Damage") || key.equals("Enchantments")) {
		//- 		return;
		//- 	}
		//- 	hashCodeBuilder.append(key).append(nbt.get(key));
		//- });
		//# end
		return hashCodeBuilder.toHashCode();
	}

	public enum NbtMatchMode {
		NONE, SOME, ALL
	}
}
