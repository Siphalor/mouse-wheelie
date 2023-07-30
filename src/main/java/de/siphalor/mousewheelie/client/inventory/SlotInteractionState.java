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
