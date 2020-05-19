package de.siphalor.mousewheelie.client.util.accessors;

import de.siphalor.mousewheelie.client.util.ScrollAction;
import net.minecraft.screen.slot.Slot;

public interface IContainerScreen {
	Slot mouseWheelie_getSlotAt(double mouseX, double mouseY);

	ScrollAction mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount);

	boolean mouseWheelie_triggerSort();
}
