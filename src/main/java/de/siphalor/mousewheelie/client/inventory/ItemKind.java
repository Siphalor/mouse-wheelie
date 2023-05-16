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
