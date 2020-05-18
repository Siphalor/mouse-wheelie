package de.siphalor.mousewheelie.client.compat;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class ModMenuEntryPoint implements ModMenuApi {
	@Override
	public String getModId() {
		return MouseWheelie.MOD_ID;
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return screen -> MWClient.tweedClothBridge.buildScreen();
	}
}
