package de.siphalor.mousewheelie.client.util.accessors;

import de.siphalor.mousewheelie.client.util.ScrollAction;

public interface ISpecialScrollableScreen {
	ScrollAction mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount);
}
