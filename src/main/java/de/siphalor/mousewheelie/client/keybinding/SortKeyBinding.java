package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class SortKeyBinding extends AmecsKeyBinding implements PriorityKeyBinding {
	public SortKeyBinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}

	@Override
	public boolean onPressed() {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen instanceof IContainerScreen)
			return ((IContainerScreen) currentScreen).mouseWheelie_triggerSort();
		return false;
	}
}
