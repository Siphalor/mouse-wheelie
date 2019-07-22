package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.ListeningKeyBinding;
import de.siphalor.mousewheelie.client.util.IContainerScreen;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class SortKeyBinding extends FabricKeyBinding implements ListeningKeyBinding {
	public SortKeyBinding(Identifier id, InputUtil.Type type, int code, String category) {
		super(id, type, code, category);
	}

	@Override
	public void onPressed() {
		Screen screen = MinecraftClient.getInstance().currentScreen;
		if(screen instanceof IContainerScreen)
			((IContainerScreen) screen).mouseWheelie_triggerSort();
	}

	@Override
	public int compareTo(KeyBinding o) {
		return method_1430(o);
	}
}
