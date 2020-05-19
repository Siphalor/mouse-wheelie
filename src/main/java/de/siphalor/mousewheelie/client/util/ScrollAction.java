package de.siphalor.mousewheelie.client.util;

public enum ScrollAction {
	PASS(false, false), SUCCESS(true, true), FAILURE(false, true), ABORT(true, false);
	boolean cancelCustomActions;
	boolean cancelAllActions;

	ScrollAction(boolean cancelCustomActions, boolean cancelAllActions) {
		this.cancelCustomActions = cancelCustomActions;
		this.cancelAllActions = cancelAllActions;
	}

	public boolean cancelsAllActions() {
		return cancelAllActions;
	}

	public boolean cancelsCustomActions() {
		return cancelCustomActions;
	}
}
