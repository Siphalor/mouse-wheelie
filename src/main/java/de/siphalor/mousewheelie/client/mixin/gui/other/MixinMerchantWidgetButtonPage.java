/*
 * Copyright 2021 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.accessors.IMerchantScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISpecialClickableButtonWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net/minecraft/client/gui/screen/ingame/MerchantScreen$WidgetButtonPage")
public class MixinMerchantWidgetButtonPage implements ISpecialClickableButtonWidget {
	@Shadow
	@Final
	int field_19165;

	@Override
	public boolean mouseClicked(int mouseButton) {
		if (mouseButton != 1 || !MWConfig.general.enableQuickCraft) return false;
		MinecraftClient minecraft = MinecraftClient.getInstance();
		Screen screen = minecraft.currentScreen;
		if (screen instanceof IMerchantScreen) {
			((IMerchantScreen) screen).mouseWheelie_setRecipeId(this.field_19165 + ((IMerchantScreen) screen).getRecipeIdOffset());
			((IMerchantScreen) screen).mouseWheelie_syncRecipeId();
			if (screen instanceof ContainerScreen) {
				if (Screen.hasShiftDown())
					InteractionManager.pushClickEvent(((ContainerScreen<?>) screen).getContainer().syncId, 2, 1, SlotActionType.QUICK_MOVE);
				else
					InteractionManager.pushClickEvent(((ContainerScreen<?>) screen).getContainer().syncId, 2, 1, SlotActionType.PICKUP);
			}
		}

		return true;
	}
}
