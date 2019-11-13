package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookWidget.class)
public class MixinRecipeBookWidget {
	@Shadow protected CraftingContainer<?> craftingContainer;

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickRecipe(ILnet/minecraft/recipe/Recipe;Z)V", shift = At.Shift.AFTER))
	public void mouseClicked(double x, double y, int mouseButton, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(Config.enableQuickCraft.value && mouseButton == 1) {
			InteractionManager.pushClickEvent(craftingContainer.syncId, craftingContainer.getCraftingResultSlotIndex(), 0, Screen.hasShiftDown() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
		}
	}
}
