package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.Core;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class ModMenuEntryPoint implements ModMenuApi {
	@Override
	public String getModId() {
		return Core.MOD_ID;
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return screen -> ClientCore.tweedClothBridge.buildScreen();
	}
}
