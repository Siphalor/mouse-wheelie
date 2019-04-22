package de.siphalor.mousewheelie.client.mixin;

import net.minecraft.client.gui.recipebook.RecipeBookGuiResults;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookGuiResults.class)
public interface RecipeBookGuiResultsAccessor {
	@Accessor
	int getCurrentPage();

	@Accessor
	void setCurrentPage(int page);

	@Accessor
	int getPageCount();

	@Invoker
	void callRefreshResultButtons();

}
