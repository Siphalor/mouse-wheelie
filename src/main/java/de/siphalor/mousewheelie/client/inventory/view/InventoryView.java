/*
 * Copyright 2026 Siphalor and contributors
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

package de.siphalor.mousewheelie.client.inventory.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.Value;
import org.jspecify.annotations.NonNull;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public interface InventoryView extends Iterable<InventoryViewEntry> {
	static InventoryView ofContainer(Container container) {
		return ofContainerRange(container, 0, container.getContainerSize());
	}

	static InventoryView ofContainerRange(Container container, int startSlot, int endSlot) {
		return new InventoryView() {
			@Override
			public @NonNull Iterator<InventoryViewEntry> iterator() {
				return new Iterator<>() {
					private int index = startSlot;

					@Override
					public boolean hasNext() {
						return index < endSlot;
					}

					@Override
					public InventoryViewEntry next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						ItemStack stack = container.getItem(index);
						InventoryViewEntry entry = new InventoryViewEntry(new InventoryViewLocation.Simple(index), stack);
						index++;
						return entry;
					}
				};
			}
		};
	}

	//# if MC_VERSION_NUMBER >= 12103
	static InventoryView appendingBundles(InventoryView inner) {
		@Value
		class BundleEntry {
			int invIndex;
			BundleContents contents;
		}

		return new InventoryView() {
			private List<BundleEntry> bundleEntries;

			@Override
			public @NonNull Iterator<InventoryViewEntry> iterator() {
				Iterator<InventoryViewEntry> innerIterator = inner.iterator();
				return new Iterator<>() {
					private int bundleIndex = 0;
					private int bundleContentsIndex = 0;

					@Override
					public boolean hasNext() {
						if (innerIterator.hasNext()) {
							return true;
						} else if (bundleEntries == null) {
							collectBundleEntries();
						}
						return bundleIndex < bundleEntries.size()
								&& bundleContentsIndex < bundleEntries.get(bundleIndex).contents.size();
					}

					@Override
					public InventoryViewEntry next() {
						if (innerIterator.hasNext()) {
							return innerIterator.next();
						} else if (bundleEntries == null) {
							collectBundleEntries();
						} else if (bundleIndex < bundleEntries.size()) {
							BundleEntry bundleEntry = bundleEntries.get(bundleIndex);

							InventoryViewEntry entry = new InventoryViewEntry(
									new InventoryViewLocation.Bundle(bundleEntry.getInvIndex(), bundleContentsIndex),
									bundleEntry.getContents()
											//# if MC_VERSION_NUMBER >= 260300
											.itemCopies()
											.skip(bundleContentsIndex)
											.findFirst().orElse(ItemStack.EMPTY)
											//# elif MC_VERSION_NUMBER >= 260100
											//- .itemCopyStream()
											//- .skip(bundleContentsIndex)
											//- .findFirst().orElse(ItemStack.EMPTY)
											//# else
											//- .getItemUnsafe(bundleContentsIndex)
											//# end
							);

							bundleContentsIndex++;
							if (bundleContentsIndex >= bundleEntry.getContents().size()) {
								bundleIndex++;
								bundleContentsIndex = 0;
							}
							return entry;
						}
						throw new NoSuchElementException();
					}
				};
			}

			private void collectBundleEntries() {
				bundleEntries = new ArrayList<>();
				for (InventoryViewEntry entry : inner) {
					if (entry.getStack().getItem() instanceof BundleItem) {
						BundleContents contents = entry.getStack().getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
						if (!contents.isEmpty()) {
							bundleEntries.add(new BundleEntry(
									entry.getLocation().getInventorySlotId(),
									contents
							));
						}
					}
				}
			}
		};
	}
	//# end
}
