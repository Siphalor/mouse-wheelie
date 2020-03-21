package de.siphalor.mousewheelie.client.compat;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.ClientCore;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ModMenuEntryPoint implements ModMenuApi {
	@Override
	public String getModId() {
		return MouseWheelie.MOD_ID;
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return screen -> ClientCore.tweedClothBridge.buildScreen();
	}
}
