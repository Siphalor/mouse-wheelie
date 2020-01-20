package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.client.ClientCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class OpenConfigScreenKeybinding extends AmecsKeyBinding implements PriorityKeyBinding {
	public OpenConfigScreenKeybinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}

	@Override
	public boolean onPressedPriority() {
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		if (minecraftClient.currentScreen == null || minecraftClient.currentScreen instanceof AbstractContainerScreen)
			minecraftClient.openScreen(ClientCore.tweedClothBridge.buildScreen());
		return true;
	}
}
