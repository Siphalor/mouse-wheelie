package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.util.IRecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeBookResults.class)
public abstract class MixinRecipeBookResults implements IRecipeBookResults {
	@Shadow private int currentPage;

	@Shadow private int pageCount;

	@Shadow protected abstract void refreshResultButtons();

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
}
