package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.MWClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class ScrollKeyBinding extends AmecsKeyBinding implements PriorityKeyBinding {
	private final boolean scrollDown;

	public ScrollKeyBinding(Identifier id, String category, boolean scrollDown) {
		super(id, InputUtil.Type.MOUSE, scrollDown ? KeyBindingUtils.MOUSE_SCROLL_DOWN : KeyBindingUtils.MOUSE_SCROLL_UP, category, KeyModifiers.NONE);
		this.scrollDown = scrollDown;
	}

	@Override
	public boolean onPressed() {
		return MWClient.triggerScroll(MWClient.getMouseX(), MWClient.getMouseY(), scrollDown ? Config.scrollFactor.value : -Config.scrollFactor.value);
	}
}
