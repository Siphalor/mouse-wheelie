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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.util.inject.ISpecialScrollableScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.KeyEvent;

//# if MC_VERSION_NUMBER >= 12103
@Mixin(AbstractRecipeBookScreen.class)
public class MixinAbstractRecipeBookScreen implements ISpecialScrollableScreen {
	@Shadow
	@Final
	private RecipeBookComponent<?> recipeBookComponent;

	@Override
	public ScrollAction mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		return ((IRecipeBookWidget) recipeBookComponent).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
	}

	//# if MC_VERSION_NUMBER >= 260400
	@Inject(method = "keyPressed", at = @At("TAIL"), cancellable = true)
	public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		// Pass the drop key through as it happened in older MC versions
		if (recipeBookComponent.isVisible()
				&& Minecraft.getInstance().options.keyDrop.matches(event)
				&& recipeBookComponent.keyPressed(event)) {
			cir.setReturnValue(true);
		}
	}
	//# end
}
//# end
