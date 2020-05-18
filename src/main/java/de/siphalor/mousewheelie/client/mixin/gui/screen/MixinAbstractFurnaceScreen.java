package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IRecipeBookScreen;
import de.siphalor.mousewheelie.client.util.accessors.IScrollableRecipeBook;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceScreen.class)
public abstract class MixinAbstractFurnaceScreen extends AbstractContainerScreen implements IScrollableRecipeBook {

	@Shadow
	@Final
	public AbstractFurnaceRecipeBookScreen recipeBook;

	public MixinAbstractFurnaceScreen(Container container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		return ((IRecipeBookScreen) recipeBook).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
	}
}
