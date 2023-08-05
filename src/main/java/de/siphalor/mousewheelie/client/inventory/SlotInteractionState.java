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

/**
 * Models the internal interaction state of a slot.
 */
public enum SlotInteractionState {
	/**
	 * The slot's default state.
	 */
	NORMAL,
	/**
	 * Indicates that the slot is still interactable,
	 * but it is not safe to rely on the amount of the stack, as that is currently in flux.
	 */
	UNSTABLE_AMOUNT,
	/**
	 * A slot is temporarily locked for interactions.
	 * Used to stop bulk actions from interfering with each other.
	 */
	TEMP_LOCKED,
	;

	/**
	 * Determines whether new interactions for this slot may be registered.
	 * @return whether new interactions may be registered.
	 */
	boolean areInteractionsLocked() {
		return this == TEMP_LOCKED;
	}

	/**
	 * Determines whether the current stack amount may be relied upon or is in flux.
	 * @return whether the current stack amount is stable.
	 */
	boolean isAmountStable() {
		return this == NORMAL;
	}
}
