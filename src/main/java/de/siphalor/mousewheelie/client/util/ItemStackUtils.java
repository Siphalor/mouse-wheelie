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

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Iterator;

public class ItemStackUtils {
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
		if (a.hasCustomName()) {
			if (!b.hasCustomName()) {
				return -1;
			}
			return compareEqualItems3(a, b);
		}
		if (b.hasCustomName()) {
			return 1;
		}
		return compareEqualItems3(a, b);
	}

	private static int compareEqualItems3(ItemStack a, ItemStack b) {
		// compare tooltips
		Iterator<Text> tooltipsA = a.getTooltip(null, TooltipContext.Default.NORMAL).iterator();
		Iterator<Text> tooltipsB = b.getTooltip(null, TooltipContext.Default.NORMAL).iterator();

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

	private static int compareEqualItems4(ItemStack a, ItemStack b) {
		// compare damage
		return Integer.compare(a.getDamage(), b.getDamage());
	}
}
