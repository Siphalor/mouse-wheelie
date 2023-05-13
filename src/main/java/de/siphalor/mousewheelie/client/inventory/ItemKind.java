package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import net.minecraft.item.ItemStack;

public class ItemKind {
	private final ItemStack stack;

	private ItemKind(ItemStack stack) {
		this.stack = stack;
	}

	public static ItemKind of(ItemStack stack) {
		return new ItemKind(stack);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemKind that = (ItemKind) o;
		return ItemStackUtils.areItemsOfSameKind(stack, that.stack, MWConfig.general.itemKindsNbtMatchMode);
	}

	@Override
	public int hashCode() {
		return ItemStackUtils.hashByKind(stack, MWConfig.general.itemKindsNbtMatchMode);
	}
}
