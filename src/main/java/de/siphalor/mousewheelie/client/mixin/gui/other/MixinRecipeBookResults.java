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
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookResults;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.context.ContextMap;
//- import net.minecraft.world.item.crafting.Recipe;
//- import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

@Environment(EnvType.CLIENT)
@Mixin(RecipeBookPage.class)
public abstract class MixinRecipeBookResults implements IRecipeBookResults {
	@Shadow
	private int currentPage;

	@Shadow
	private int totalPages;

	@Shadow
	protected abstract void updateButtonsForPage();

	//# if MC_VERSION_NUMBER >= 12103
	@Shadow
	private RecipeDisplayId lastClickedRecipe;
	//# elif MC_VERSION_NUMBER >= 12002
	//- @Shadow
	//- private RecipeHolder<?> lastClickedRecipe;
	//# else
	//- @Shadow
	//- private Recipe<?> lastClickedRecipe;
	//# end

	@Shadow
	private RecipeCollection lastClickedRecipeCollection;

	@Override
	public void mouseWheelie_setCurrentPage(int page) {
		currentPage = page;
	}

	@Override
	public int mouseWheelie_getCurrentPage() {
		return currentPage;
	}

	@Override
	public int mouseWheelie_getPageCount() {
		return totalPages;
	}

	@Override
	public void mouseWheelie_refreshResultButtons() {
		updateButtonsForPage();
	}

	@Inject(
			method = "mouseClicked",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeButton;isOnlyOption()Z")),
			at = @At(value = "CONSTANT", args = "intValue=1"),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	public void mouseClicked(
			//# if MC_VERSION_NUMBER >= 12109
			MouseButtonEvent event,
			//# else
			//- double mouseX, double mouseY, int button,
			//# end
			int areaLeft, int areaTop, int areaWidth, int areaHeight,
			//# if MC_VERSION_NUMBER >= 12109
			boolean doubleClick,
			//# end
			CallbackInfoReturnable<Boolean> cir,
			//# if MC_VERSION_NUMBER >= 12103
			ContextMap contextMap,
			//# end
			Iterator<?> iterator, RecipeButton recipeButton
	) {
		//# if MC_VERSION_NUMBER >= 12109
		int button = event.button();
		//# end
		//# if MC_VERSION_NUMBER >= 260300
		if (MouseWheelie.config.general.enableQuickCraft && button == InputConstants.MOUSE_BUTTON_RIGHT && recipeButton.isOnlyOption()) {
		//# else
		//- if (MouseWheelie.config.general.enableQuickCraft && button == 1 && recipeButton.isOnlyOption()) {
		//# end
			//# if MC_VERSION_NUMBER >= 12103
			lastClickedRecipe = recipeButton.getCurrentRecipe();
			//# else
			//- lastClickedRecipe = recipeButton.getRecipe();
			//# end
			lastClickedRecipeCollection = recipeButton.getCollection();
		}
	}
}
