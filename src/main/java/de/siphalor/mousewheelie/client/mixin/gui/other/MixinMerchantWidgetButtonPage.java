/*
 * Copyright 2020 Siphalor and contributors
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

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.inject.IMerchantScreen;
import de.siphalor.mousewheelie.client.util.inject.ISpecialClickableButtonWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//- import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//- import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerInput;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net/minecraft/client/gui/screens/inventory/MerchantScreen$TradeOfferButton")
public class MixinMerchantWidgetButtonPage implements ISpecialClickableButtonWidget {
	@Shadow
	@Final
	int index;

	@Override
	public boolean mouseWheelie_mouseClickedSpecial(int mouseButton) {
		//# if MC_VERSION_NUMBER >= 260300
		if (mouseButton != InputConstants.MOUSE_BUTTON_RIGHT || !MouseWheelie.config.general.enableQuickCraft) return false;
		//# else
		//- if (mouseButton != 1 || !MouseWheelie.config.general.enableQuickCraft) return false;
		//# end
		Screen screen = MWClient.getOpenScreen();
		if (screen instanceof IMerchantScreen) {
			((IMerchantScreen) screen).mouseWheelie_setRecipeId(this.index + ((IMerchantScreen) screen).mouseWheelie_getRecipeIdOffset());
			((IMerchantScreen) screen).mouseWheelie_syncRecipeId();
			if (screen instanceof AbstractContainerScreen) {
				if (MWClient.WHOLE_STACK_MODIFIER.isDown())
					InteractionManager.pushClickEvent(
							((AbstractContainerScreen<?>) screen).getMenu().containerId,
							2,
							1,
							//# if MC_VERSION_NUMBER >= 260100
							ContainerInput.QUICK_MOVE
							//# else
							//- ClickType.QUICK_MOVE
							//# end
					);
				else
					InteractionManager.pushClickEvent(
							((AbstractContainerScreen<?>) screen).getMenu().containerId,
							2,
							1,
							//# if MC_VERSION_NUMBER >= 260100
							ContainerInput.PICKUP
							//# else
							//- ClickType.PICKUP
							//# end
					);
			}
		}

		return true;
	}
}
