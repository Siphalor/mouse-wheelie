package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.util.IRecipeBookGui;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.container.AbstractFurnaceRecipeBookScreen;
import net.minecraft.client.gui.container.AbstractFurnaceScreen;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceScreen.class)
public abstract class MixinAbstractFurnaceScreen extends ContainerScreen {

	@Shadow @Final public AbstractFurnaceRecipeBookScreen recipeBook;

	public MixinAbstractFurnaceScreen(Container container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		if(super.mouseScrolled(double_1, double_2, double_3))
			return true;
		return ((IRecipeBookGui) recipeBook).mouseWheelie_scroll(double_1, double_2, double_3);
	}
}
