package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.util.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IScrollableRecipeBook;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceScreen.class)
public abstract class MixinAbstractFurnaceScreen extends HandledScreen<AbstractFurnaceScreenHandler> implements IScrollableRecipeBook {

	@Shadow
	@Final
	public AbstractFurnaceRecipeBookScreen recipeBook;

	public MixinAbstractFurnaceScreen(AbstractFurnaceScreenHandler container, PlayerInventory playerInventory, Text name) {
		super(container, playerInventory, name);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		return ((IRecipeBookWidget) recipeBook).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
	}
}
