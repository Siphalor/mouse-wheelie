package de.siphalor.mousewheelie.client.util.accessors;

import net.minecraft.screen.slot.Slot;

public interface IContainerScreen {
	Slot mouseWheelie_getSlotAt(double mouseX, double mouseY);

	boolean mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount);

	boolean mouseWheelie_triggerSort();
}
