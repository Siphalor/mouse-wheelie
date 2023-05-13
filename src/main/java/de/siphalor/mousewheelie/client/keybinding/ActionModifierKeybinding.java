package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class ActionModifierKeybinding extends AmecsKeyBinding implements PriorityKeyBinding {
	public ActionModifierKeybinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}

	@Override
	public boolean onPressedPriority() {
		setPressed(true);
		return false;
	}

	@Override
	public boolean onReleasedPriority() {
		setPressed(false);
		return false;
	}
}
