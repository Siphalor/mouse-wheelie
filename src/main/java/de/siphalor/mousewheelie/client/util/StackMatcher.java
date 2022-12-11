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

package de.siphalor.mousewheelie.client.util;

import com.google.common.base.Objects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StackMatcher {
	private final @NotNull Item item;
	private final @Nullable NbtCompound nbt;

	private StackMatcher(@NotNull Item item, @Nullable NbtCompound nbt) {
		this.item = item;
		this.nbt = nbt;
	}

	public static StackMatcher ignoreNbt(@NotNull ItemStack stack) {
		return new StackMatcher(stack.getItem(), null);
	}

	public static StackMatcher of(@NotNull ItemStack stack) {
		return new StackMatcher(stack.getItem(), stack.getNbt());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StackMatcher other) {
			return item == other.item && Objects.equal(nbt, other.nbt);
		}
		if (obj instanceof ItemStack stack) {
			return item == stack.getItem() && Objects.equal(nbt, stack.getNbt());
		}
		if (obj instanceof Item objItem) {
			return item == objItem;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(item, nbt);
	}
}
