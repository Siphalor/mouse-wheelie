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

package de.siphalor.mousewheelie.client.util.inject;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ISlot {
	/**
	 * The index of the slot in its inventory.
	 * As a container may have several inventories, multiple slots may use the same index within a container.
	 * @return the index within the inventory
	 * @see net.minecraft.screen.slot.Slot#index
	 */
	int mouseWheelie_getIndexInInv();

	/**
	 * The unique id of the slot within its container.
	 * This is unique within a container, but may differ from the index in the inventory.
	 * @return the unique id within the container
	 * @see net.minecraft.screen.slot.Slot#id
	 */
	int mouseWheelie_getIdInContainer();
}
