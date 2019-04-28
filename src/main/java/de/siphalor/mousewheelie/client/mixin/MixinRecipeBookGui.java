package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.Core;
import de.siphalor.mousewheelie.util.IRecipeBookGui;
import net.minecraft.client.gui.recipebook.GroupButtonWidget;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookGuiResults;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RecipeBookGui.class)
public abstract class MixinRecipeBookGui implements IRecipeBookGui {

	@Shadow protected RecipeBookGuiResults recipesArea;

	@Shadow private int parentWidth;

	@Shadow private int leftOffset;

	@Shadow @Final private List<GroupButtonWidget> tabButtons;

	@Shadow private GroupButtonWidget currentTab;

	@Shadow protected abstract void refreshResults(boolean boolean_1);

	@Shadow private int parentHeight;

	@Override
	public boolean mouseWheelie_scrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		int top = (this.parentHeight - 166) / 2;
		if(mouseY < top || mouseY >= top + 166)
			return false;
		int left = (this.parentWidth - 147) / 2 - this.leftOffset;
		if(mouseX >= left && mouseX < left + 147) {
			// Ugly approach since assigning the casted value causes a runtime mixin error
			((RecipeBookGuiResultsAccessor) recipesArea).setCurrentPage(MathHelper.clamp((int) (((RecipeBookGuiResultsAccessor) recipesArea).getCurrentPage() + Math.round(scrollAmount * Core.scrollFactor)), 0, ((RecipeBookGuiResultsAccessor) recipesArea).getPageCount() - 1));
			((RecipeBookGuiResultsAccessor) recipesArea).callRefreshResultButtons();
			return true;
		} else if(mouseX >= left - 30 && mouseX < left) {
			int index = tabButtons.indexOf(currentTab);
			int newIndex = MathHelper.clamp(index + (int) (Math.round(scrollAmount * Core.scrollFactor)), 0, tabButtons.size() - 1);
			if(newIndex != index) {
				currentTab.setToggled(false);
				currentTab = tabButtons.get(newIndex);
				currentTab.setToggled(true);
				refreshResults(true);
			}
			return true;
		}
		return false;
	}
}
