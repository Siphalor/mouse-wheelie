package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.ClientCore;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class ScrollKeyBinding extends AmecsKeyBinding implements PriorityKeyBinding {
	private final boolean scrollDown;

	public ScrollKeyBinding(Identifier id, String category, boolean scrollDown) {
		super(id, InputUtil.Type.MOUSE, scrollDown ? KeyBindingUtils.MOUSE_SCROLL_DOWN : KeyBindingUtils.MOUSE_SCROLL_UP, category, new KeyModifiers());
		this.scrollDown = scrollDown;
	}

	@Override
	public boolean onPressedPriority() {
		return ClientCore.triggerScroll(ClientCore.getMouseX(), ClientCore.getMouseY(), scrollDown ? MouseWheelie.CONFIG.general.scrollFactor : -MouseWheelie.CONFIG.general.scrollFactor);
	}
}
