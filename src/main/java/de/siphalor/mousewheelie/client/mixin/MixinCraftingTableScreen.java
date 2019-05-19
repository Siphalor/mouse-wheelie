package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.util.IRecipeBookGui;
import de.siphalor.mousewheelie.client.util.IScrollableRecipeBook;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.CraftingTableScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookScreen;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingTableScreen.class)
public abstract class MixinCraftingTableScreen extends AbstractContainerScreen<CraftingTableContainer> implements IScrollableRecipeBook {
	@Shadow @Final private RecipeBookScreen recipeBookGui;

	public MixinCraftingTableScreen(CraftingTableContainer container_1, PlayerInventory playerInventory_1, Component textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public boolean mouseWheelie_onMouseScrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		return ((IRecipeBookGui) recipeBookGui).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
	}
}
