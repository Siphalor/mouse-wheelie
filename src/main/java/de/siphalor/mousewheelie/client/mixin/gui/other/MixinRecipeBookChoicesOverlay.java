/*
 * Copyright 2025 Siphalor and contributors
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

@Mixin(OverlayRecipeComponent.class)
public class MixinRecipeBookChoicesOverlay {
	@ModifyVariable(
			method = "mouseClicked",
			at = @At("HEAD"),
			argsOnly = true
	)
	//# if MC_VERSION_NUMBER >= 260300
	public MouseButtonEvent modifyMouseClickedEvent(MouseButtonEvent event) {
		if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && MouseWheelie.config.general.enableQuickCraft) {
			return new MouseButtonEvent(event.x(), event.y(), new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, event.modifiers()));
		}
		return event;
	}
	//# elif MC_VERSION_NUMBER >= 12109
	//- public MouseButtonEvent modifyMouseClickedEvent(MouseButtonEvent event) {
	//- 	if (event.button() == 1 && MouseWheelie.config.general.enableQuickCraft) {
	//- 		return new MouseButtonEvent(event.x(), event.y(), new MouseButtonInfo(0, event.modifiers()));
	//- 	}
	//- 	return event;
	//- }
	//# else
	//- public int modifyMouseClickedButton(int button) {
	//- 	if (button == 1 && MouseWheelie.config.general.enableQuickCraft) {
	//- 		return 0;
	//- 	}
	//- 	return button;
	//- }
	//# end
}
