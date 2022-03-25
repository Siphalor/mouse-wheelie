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

import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Additional context for executing a sort.
 *
 * @see SortMode#sort(int[], ItemStack[], SortContext)
 */
public class SortContext {
	private final ContainerScreen<?> screen;
	private final List<Slot> relevantSlots;

	public SortContext(ContainerScreen<?> screen, List<Slot> relevantSlots) {
		this.screen = screen;
		this.relevantSlots = relevantSlots;
	}

	/**
	 * Gets the screen that is currently sorted on.
	 * @return The screen
	 */
	public ContainerScreen<?> getScreen() {
		return screen;
	}

	/**
	 * Gets the slots that are the target of the current sort action.
	 * These slots are usually in the same scope (see {@link de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper#getScope(Slot)}).
	 * @return The relevant slots
	 */
	public List<Slot> getRelevantSlots() {
		return relevantSlots;
	}
}
