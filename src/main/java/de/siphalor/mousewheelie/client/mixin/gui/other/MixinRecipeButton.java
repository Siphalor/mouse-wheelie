/*
 * Copyright 2026 Siphalor and contributors
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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
//- import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.recipe_book.RecipeBookQuickCraftHandler;
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookRecipeButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

//# if MC_VERSION_NUMBER >= 260400
@Mixin(RecipeButton.class)
public abstract class MixinRecipeButton extends AbstractButton implements IRecipeBookRecipeButton {
	@Shadow
	public abstract void onPress(InputWithModifiers input);

	@Unique
	private RecipeBookQuickCraftHandler quickCraftHandler;

	public MixinRecipeButton(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Override
	public void mouseWheelie_setQuickCraftHandler(RecipeBookQuickCraftHandler quickCraftHandler) {
		this.quickCraftHandler = quickCraftHandler;
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void onMouseClicked(
			MouseButtonEvent event,
			boolean doubleClick,
			CallbackInfoReturnable<Boolean> cir,
			@Share("event") LocalRef<MouseButtonEvent> sharedEvent
	) {
		sharedEvent.set(event);
	}

	@WrapOperation(
			method = "mouseClicked",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeButton;openOverlay()Z"
			)
	)
	private boolean onRightClicked(
			RecipeButton instance,
			Operation<Boolean> original,
			@Share("event") LocalRef<MouseButtonEvent> sharedEvent
	) {
		boolean overlayOpened = original.call(instance);
		if (overlayOpened) {
			return true;
		}
		if (!isMouseOver(sharedEvent.get().x(), sharedEvent.get().y())) {
			return false;
		}
		playDownSound(Minecraft.getInstance().getSoundManager());
		onPress(sharedEvent.get());
		quickCraftHandler.triggerQuickCraft();
		return false;
	}
}
//# end
