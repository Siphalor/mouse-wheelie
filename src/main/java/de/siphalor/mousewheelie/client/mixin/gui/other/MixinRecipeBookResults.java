package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.util.accessors.IRecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(RecipeBookResults.class)
public abstract class MixinRecipeBookResults implements IRecipeBookResults {
	@Shadow
	private int currentPage;

	@Shadow
	private int pageCount;

	@Shadow
	protected abstract void refreshResultButtons();

	@Shadow
	private Recipe<?> lastClickedRecipe;

	@Shadow
	private RecipeResultCollection resultCollection;

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
		return pageCount;
	}

	@Override
	public void mouseWheelie_refreshResultButtons() {
		refreshResultButtons();
	}

	@Inject(method = "mouseClicked", at = @At(value = "JUMP", opcode = 154), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void mouseClicked(double x, double y, int mouseButton, int int2, int int3, int int4, int int5, CallbackInfoReturnable<Boolean> callbackInfoReturnable, Iterator iterator, AnimatedResultButton animatedResultButton) {
		if (Config.enableQuickCraft.value && mouseButton == 1 && animatedResultButton.hasResults()) {
			lastClickedRecipe = animatedResultButton.currentRecipe();
			resultCollection = animatedResultButton.getResultCollection();
		}
	}
}
