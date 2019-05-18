package de.siphalor.mousewheelie.client.mixin;

import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookResults.class)
public interface RecipeBookResultsAccessor {
	@Accessor
	int getCurrentPage();

	@Accessor
	void setCurrentPage(int page);

	@Accessor
	int getPageCount();

	@Invoker
	void callRefreshResultButtons();

}
