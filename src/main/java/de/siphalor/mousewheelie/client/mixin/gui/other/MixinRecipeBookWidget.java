package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IRecipeBookResults;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(RecipeBookWidget.class)
public abstract class MixinRecipeBookWidget implements IRecipeBookWidget {

	@Shadow @Final protected RecipeBookResults recipesArea;

	@Shadow private int parentWidth;

	@Shadow private int leftOffset;

	@Shadow @Final private List<RecipeGroupButtonWidget> tabButtons;

	@Shadow private RecipeGroupButtonWidget currentTab;

	@Shadow protected abstract void refreshResults(boolean boolean_1);

	@Shadow
	private int parentHeight;

	@Shadow
	public abstract boolean isOpen();

	@Shadow
	private boolean searching;

	@Shadow
	protected MinecraftClient client;

	@Shadow
	@Final
	protected RecipeFinder recipeFinder;

	@Shadow
	protected AbstractRecipeScreenHandler<?> craftingScreenHandler;

	@Override
	public ScrollAction mouseWheelie_scrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		if (!this.isOpen())
			return ScrollAction.PASS;
		int top = (this.parentHeight - 166) / 2;
		if (mouseY < top || mouseY >= top + 166)
			return ScrollAction.PASS;
		int left = (this.parentWidth - 147) / 2 - this.leftOffset;
		if (mouseX >= left && mouseX < left + 147) {
			// Ugly approach since assigning the casted value causes a runtime mixin error
			int maxPage = ((IRecipeBookResults) recipesArea).mouseWheelie_getPageCount() - 1;
			((IRecipeBookResults) recipesArea).mouseWheelie_setCurrentPage(MathHelper.clamp((int) (((IRecipeBookResults) recipesArea).mouseWheelie_getCurrentPage() + Math.round(scrollAmount * MWConfig.scrolling.scrollFactor)), 0, Math.max(maxPage, 0)));
			((IRecipeBookResults) recipesArea).mouseWheelie_refreshResultButtons();
			return ScrollAction.SUCCESS;
		} else if(mouseX >= left - 30 && mouseX < left) {
			int index = tabButtons.indexOf(currentTab);
			int newIndex = MathHelper.clamp(index + (int) (Math.round(scrollAmount * MWConfig.scrolling.scrollFactor)), 0, tabButtons.size() - 1);
			if (newIndex != index) {
				currentTab.setToggled(false);
				currentTab = tabButtons.get(newIndex);
				currentTab.setToggled(true);
				refreshResults(true);
			}
			return ScrollAction.SUCCESS;
		}
		return ScrollAction.PASS;
	}

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickRecipe(ILnet/minecraft/recipe/Recipe;Z)V", shift = At.Shift.AFTER))
	public void mouseClicked(double x, double y, int mouseButton, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (MWConfig.general.enableQuickCraft & mouseButton == 1) {
			int resSlot = craftingScreenHandler.getCraftingResultSlotIndex();
			Recipe<?> recipe = recipesArea.getLastClickedRecipe();
			if (canCraftMore(recipe)) {
				InteractionManager.clear();
				InteractionManager.setWaiter((InteractionManager.TriggerType triggerType) -> MWClient.lastUpdatedSlot >= craftingScreenHandler.getCraftingSlotCount());
			}
			InteractionManager.pushClickEvent(craftingScreenHandler.syncId, resSlot, 0, Screen.hasShiftDown() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void keyPressed(int int1, int int2, int int3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (MWConfig.general.enableQuickCraft && isOpen() && !client.player.isSpectator()) {
			if (MinecraftClient.getInstance().options.keyDrop.matchesKey(int1, int2)) {
				searching = false;
				Recipe<?> oldRecipe = recipesArea.getLastClickedRecipe();
				if (this.recipesArea.mouseClicked(MWClient.getMouseX(), MWClient.getMouseY(), 0, (this.parentWidth - 147) / 2 - this.leftOffset, (this.parentHeight - 166) / 2, 147, 166)) {
					Recipe<?> recipe = recipesArea.getLastClickedRecipe();
					RecipeResultCollection resultCollection = recipesArea.getLastClickedResults();
					if (!resultCollection.isCraftable(recipe)) {
						return;
					}
					int resSlot = craftingScreenHandler.getCraftingResultSlotIndex();
					if (Screen.hasControlDown()) {
						if (oldRecipe != recipe || craftingScreenHandler.slots.get(resSlot).getStack().isEmpty() || canCraftMore(recipe)) {
							InteractionManager.push(new InteractionManager.PacketEvent(new CraftRequestC2SPacket(craftingScreenHandler.syncId, recipe, true), (triggerType) -> MWClient.lastUpdatedSlot >= craftingScreenHandler.getCraftingSlotCount()));
						}
						int cnt = recipeFinder.countRecipeCrafts(recipe, recipe.getOutput().getMaxCount(), null);
						for (int i = 1; i < cnt; i++) {
							InteractionManager.pushClickEvent(craftingScreenHandler.syncId, resSlot, 1, SlotActionType.THROW);
						}
					} else {
						if (oldRecipe != recipe || craftingScreenHandler.slots.get(resSlot).getStack().isEmpty()) {
							InteractionManager.push(new InteractionManager.PacketEvent(new CraftRequestC2SPacket(craftingScreenHandler.syncId, recipe, false), (triggerType) -> MWClient.lastUpdatedSlot >= craftingScreenHandler.getCraftingSlotCount()));
						}
					}
					InteractionManager.push(new InteractionManager.CallbackEvent(() -> {
						client.interactionManager.clickSlot(craftingScreenHandler.syncId, craftingScreenHandler.getCraftingResultSlotIndex(), 0, SlotActionType.THROW, client.player);
						refreshResults(false);
						return new InteractionManager.GuiConfirmWaiter(1);
					}));
					callbackInfoReturnable.setReturnValue(true);
				}
			}
		}
	}

	@Unique
	private boolean canCraftMore(Recipe<?> recipe) {
		return getBiggestCraftingStackSize() < recipeFinder.countRecipeCrafts(recipe, recipe.getOutput().getMaxCount(), null);
	}

	@Unique
	private int getBiggestCraftingStackSize() {
		int resSlot = craftingScreenHandler.getCraftingResultSlotIndex();
		int cnt = 0;
		for (int i = 0; i < craftingScreenHandler.getCraftingSlotCount(); i++) {
			if (i == resSlot) continue;
			cnt = Math.max(cnt, craftingScreenHandler.slots.get(i).getStack().getCount());
		}
		return cnt;
	}
}
