package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.accessors.IMerchantScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISpecialClickableButtonWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net/minecraft/client/gui/screen/ingame/MerchantScreen$WidgetButtonPage")
public class MixinMerchantWidgetButtonPage implements ISpecialClickableButtonWidget {
	@Shadow
	@Final
	int index;

	@Override
	public boolean mouseClicked(int mouseButton) {
		if (mouseButton != 1 || !MWConfig.general.enableQuickCraft) return false;
		MinecraftClient minecraft = MinecraftClient.getInstance();
		Screen screen = minecraft.currentScreen;
		if (screen instanceof IMerchantScreen) {
			((IMerchantScreen) screen).mouseWheelie_setRecipeId(this.index + ((IMerchantScreen) screen).getRecipeIdOffset());
			((IMerchantScreen) screen).mouseWheelie_syncRecipeId();
			if (screen instanceof HandledScreen) {
				if (Screen.hasShiftDown())
					InteractionManager.pushClickEvent(((HandledScreen<?>) screen).getScreenHandler().syncId, 2, 1, SlotActionType.QUICK_MOVE);
				else
					InteractionManager.pushClickEvent(((HandledScreen<?>) screen).getScreenHandler().syncId, 2, 1, SlotActionType.PICKUP);
			}
		}

		return true;
	}
}
