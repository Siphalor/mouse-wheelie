package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.util.IRecipeBookGui;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.container.CraftingTableScreen;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingTableScreen.class)
public abstract class MixinCraftingTableScreen extends ContainerScreen<CraftingTableContainer> {
	@Shadow @Final private RecipeBookGui recipeBookGui;

	public MixinCraftingTableScreen(CraftingTableContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
		if(super.mouseScrolled(mouseX, mouseY, scrollAmount))
			return true;
		return ((IRecipeBookGui) recipeBookGui).mouseWheelie_scroll(mouseX, mouseY, scrollAmount);
	}
}
