package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.client.ClientCore;
import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.util.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.accessors.IRecipeBookResults;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.SlotActionType;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeBookWidget.class)
public abstract class MixinRecipeBookWidget implements IRecipeBookWidget {

	@Shadow @Final protected RecipeBookResults recipesArea;

	@Shadow private int parentWidth;

	@Shadow private int leftOffset;

	@Shadow @Final private List<RecipeGroupButtonWidget> tabButtons;

	@Shadow private RecipeGroupButtonWidget currentTab;

	@Shadow protected abstract void refreshResults(boolean boolean_1);

	@Shadow private int parentHeight;

	@Shadow public abstract boolean isOpen();

	@Shadow protected CraftingContainer<?> craftingContainer;

	@Shadow private boolean field_3087;

	@Shadow protected MinecraftClient client;

	@Shadow public abstract boolean mouseClicked(double double_1, double double_2, int int_1);

	@Override
	public boolean mouseWheelie_scrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		if(!this.isOpen())
			return false;
		int top = (this.parentHeight - 166) / 2;
		if(mouseY < top || mouseY >= top + 166)
			return false;
		int left = (this.parentWidth - 147) / 2 - this.leftOffset;
		if(mouseX >= left && mouseX < left + 147) {
			// Ugly approach since assigning the casted value causes a runtime mixin error
			int maxPage = ((IRecipeBookResults) recipesArea).mouseWheelie_getPageCount() - 1;
			((IRecipeBookResults) recipesArea).mouseWheelie_setCurrentPage(MathHelper.clamp((int) (((IRecipeBookResults) recipesArea).mouseWheelie_getCurrentPage() + Math.round(scrollAmount * Config.scrollFactor.value)), 0, maxPage < 0 ? 0 : maxPage));
			((IRecipeBookResults) recipesArea).mouseWheelie_refreshResultButtons();
			return true;
		} else if(mouseX >= left - 30 && mouseX < left) {
			int index = tabButtons.indexOf(currentTab);
			int newIndex = MathHelper.clamp(index + (int) (Math.round(scrollAmount * Config.scrollFactor.value)), 0, tabButtons.size() - 1);
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

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickRecipe(ILnet/minecraft/recipe/Recipe;Z)V", shift = At.Shift.AFTER))
	public void mouseClicked(double x, double y, int mouseButton, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(Config.enableQuickCraft.value && mouseButton == 1) {
			InteractionManager.pushClickEvent(craftingContainer.syncId, craftingContainer.getCraftingResultSlotIndex(), 0, Screen.hasShiftDown() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void keyPressed(int int1, int int2, int int3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(isOpen() && !client.player.isSpectator()) {
			if (MinecraftClient.getInstance().options.keyDrop.matchesKey(int1, int2)) {
				field_3087 = false;
				if(mouseClicked(ClientCore.getMouseX(), ClientCore.getMouseY(), 0)) {
					InteractionManager.pushClickEvent(craftingContainer.syncId, craftingContainer.getCraftingResultSlotIndex(), 0, SlotActionType.THROW);
					callbackInfoReturnable.setReturnValue(true);
				}
			}
		}
	}
}
