package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.util.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IScrollableRecipeBook;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingScreen.class)
public abstract class MixinCraftingTableScreen extends HandledScreen<CraftingScreenHandler> implements IScrollableRecipeBook {
	@Shadow
	@Final
	private RecipeBookWidget recipeBook;

	public MixinCraftingTableScreen(CraftingScreenHandler container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		return ((IRecipeBookWidget) recipeBook).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
	}
}
