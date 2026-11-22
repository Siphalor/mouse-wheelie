/*
 * Copyright 2020 Siphalor and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client.mixin.gui.other;

//- import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.mousewheelie.MouseWheelie;
//- import de.siphalor.mousewheelie.client.MWClient;
//- import de.siphalor.mousewheelie.client.mixin.StackedItemContentsAccessor;
//- import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.recipe_book.RecipeBookQuickCraftHandler;
//- import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookResults;
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookWidget;
//- import de.siphalor.mousewheelie.client.util.inject.ISlot;
//- import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//- import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractContainerWidget;
//- import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
//- import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.KeyEvent;
//- import net.minecraft.client.input.MouseButtonEvent;
//- import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
//- import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
//- import net.minecraft.util.Mth;
//- import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
//- import net.minecraft.world.inventory.ClickType;
//- import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
//- import net.minecraft.world.item.ItemStack;
//- import net.minecraft.world.item.crafting.PlacementInfo;
//- import net.minecraft.world.item.crafting.Recipe;
//- import net.minecraft.world.item.crafting.RecipeHolder;
//- import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
//- import net.minecraft.world.item.crafting.display.SlotDisplayContext;

@Environment(EnvType.CLIENT)
@Mixin(RecipeBookComponent.class)
public abstract class MixinRecipeBookWidget
		//# if MC_VERSION_NUMBER >= 260400
		extends AbstractContainerWidget
		//# end
		implements IRecipeBookWidget {
	@Unique
	private static final int WIDGET_HEIGHT = RecipeBookComponent.IMAGE_HEIGHT;
	@Unique
	private static final int WIDGET_WIDTH = RecipeBookComponent.IMAGE_WIDTH;

	@Shadow @Final
	private RecipeBookPage recipeBookPage;

	//# if MC_VERSION_NUMBER >= 260400
	@Shadow private TabNavigationBar tabBar;

	@Shadow
	private @Nullable RecipeCollection lastRecipeCollection;

	@Shadow
	private @Nullable RecipeDisplayId lastPlacedRecipe;
	//# else
	//- @Shadow private int width;

	//- @Shadow private int height;

	//- @Shadow private int xOffset;

	//- @Shadow @Final private List<RecipeBookTabButton> tabButtons;

	//- @Shadow private RecipeBookTabButton selectedTab;
	//# end

	@Shadow
	//# if MC_VERSION_NUMBER >= 12103
	protected abstract void updateCollections(boolean resetPageNumber, boolean isFiltering);

	@Shadow
	protected abstract boolean isFiltering();
	//# else
	//- protected abstract void updateCollections(boolean resetPageNumber);
	//# end

	//# if MC_VERSION_NUMBER < 260400
	//- @Shadow
	//- public abstract boolean isVisible();
	//# end

	@Shadow
	private boolean ignoreTextInput;

	@Shadow
	protected Minecraft minecraft;

	@Shadow
	@Final
	//# if MC_VERSION_NUMBER >= 12103
	private StackedItemContents stackedContents;
	//# else
	//- private StackedContents stackedContents;
	//# end

	//# if MC_VERSION_NUMBER >= 12103
	@Shadow @Final protected RecipeBookMenu menu;
	//# elif MC_VERSION_NUMBER >= 12100
	//- @Shadow protected RecipeBookMenu<?, ?> menu;
	//# else
	//- @Shadow protected RecipeBookMenu<?> menu;
	//# end

	//# if MC_VERSION_NUMBER < 12102
	//- @Shadow
	//- private @Nullable EditBox searchBox;
	//# end

	//# if MC_VERSION_NUMBER >= 12103
	@Shadow
	protected abstract boolean isCraftingSlot(Slot slot);
	//# else
	//- @Unique
	//- private boolean isCraftingSlot(Slot slot) {
	//- 	return ((ISlot) slot).mouseWheelie_getIdInContainer() < menu.getSize();
	//- }
	//# end

	//# if MC_VERSION_NUMBER >= 12102 && MC_VERSION_NUMBER < 260400
	//- @Shadow
	//- protected abstract int getXOrigin();

	//- @Shadow
	//- protected abstract int getYOrigin();
	//# end

	@Unique
	private RecipeBookQuickCraftHandler quickCraftHandler;

	//# if MC_VERSION_NUMBER >= 260400
	public MixinRecipeBookWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}
	//# end

	//# if MC_VERSION_NUMBER >= 12103
	@Inject(method = "<init>", at = @At("RETURN"))
	//# else
	//- @Inject(method = "init", at = @At("RETURN"))
	//# end
	public void onInit(CallbackInfo ci) {
		quickCraftHandler = new RecipeBookQuickCraftHandler(
				this::isCraftingSlot,
				() -> new ScreenRectangle(
						getLeft(),
						getTop(),
						147,
						166
				),
				//# if MC_VERSION_NUMBER >= 12103
				() -> updateCollections(false, isFiltering()),
				//# else
				//- () -> updateCollections(false),
				//# end
				//# if MC_VERSION_NUMBER >= 260400
				() -> Pair.of(lastRecipeCollection, lastPlacedRecipe),
				//# else
				//- () -> Pair.of(recipeBookPage.getLastClickedRecipeCollection(), recipeBookPage.getLastClickedRecipe()),
				//# end
				menu,
				recipeBookPage,
				stackedContents
		);

		//# if MC_VERSION_NUMBER >= 260400
		((IRecipeBookResults) recipeBookPage).mouseWheelie_setQuickCraftHandler(quickCraftHandler);
		//# end
	}

	@Override
	public ScrollAction mouseWheelie_scrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		if (!this.isVisible())
			return ScrollAction.PASS;
		int top = getTop();
		if (mouseY < top || mouseY >= top + WIDGET_HEIGHT)
			return ScrollAction.PASS;
		int left = getLeft();
		if (mouseX >= left && mouseX < left + WIDGET_WIDTH) {
			// Ugly approach since assigning the casted value causes a runtime mixin error
			IRecipeBookResults castedBookPage = (IRecipeBookResults) recipeBookPage;
			int maxPage = castedBookPage.mouseWheelie_getPageCount() - 1;
			castedBookPage.mouseWheelie_setCurrentPage(
					//# if MC_VERSION_NUMBER >= 260400
					Math.clamp(
					//# else
					//- Mth.clamp(
					//# end
							(int) (castedBookPage.mouseWheelie_getCurrentPage() + Math.signum(scrollAmount)),
							0,
							Math.max(maxPage, 0)
					)
			);
			castedBookPage.mouseWheelie_refreshResultButtons();
			return ScrollAction.SUCCESS;
		} else if (mouseX >= left - 30 && mouseX < left) {
			int inc = (int) Math.signum(scrollAmount);
			//# if MC_VERSION_NUMBER >= 260400
			var currentTabButton = tabBar.currentTabButton();
			int index = currentTabButton == null ? 0 : tabBar.getTabs().indexOf(currentTabButton.tab());
			index += inc;
			if (index < 0 || index >= tabBar.getTabs().size()) {
				return ScrollAction.PASS;
			}
			tabBar.selectTab(index, false);
			//# else
			//- int index = tabButtons.indexOf(selectedTab);
			//- while (true) {
			//- 	index += inc;
			//- 	if (index < 0 || index >= tabButtons.size()) {
			//- 		return ScrollAction.PASS;
			//- 	}
			//- 	RecipeBookTabButton tab = tabButtons.get(index);
			//- 	//# if MC_VERSION_NUMBER >= 260400
			//- 	if (tab.isVisible()) {
			//- 	//# else
			//- 	if (tab.visible) {
			//- 	//# end
			//- 		if (tab != selectedTab) {
			//- 			//# if MC_VERSION_NUMBER >= 12111
			//- 			selectedTab.unselect();
			//- 			selectedTab = tab;
			//- 			selectedTab.select();
			//- 			//# else
			//- 			selectedTab.setStateTriggered(false);
			//- 			selectedTab = tab;
			//- 			selectedTab.setStateTriggered(true);
			//- 			//# end
			//- 			//# if MC_VERSION_NUMBER >= 12103
			//- 			updateCollections(true, isFiltering());
			//- 			//# else
			//- 			updateCollections(true);
			//- 			//# end
			//- 		}
			//- 		break;
			//- 	}
			//- }
			//# end
			return ScrollAction.SUCCESS;
		}
		return ScrollAction.PASS;
	}

	//# if MC_VERSION_NUMBER < 260400
	//- @Inject(
	//- 		method = "mouseClicked",
	//- 		//# if MC_VERSION_NUMBER >= 12103
	//- 		at = @At(
	//- 				value = "INVOKE",
	//- 				target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;isOffsetNextToMainGUI()Z"
	//- 		)
	//- 		//# elif MC_VERSION_NUMBER >= 12002
	//- 		at = @At(
	//- 				value = "INVOKE",
	//- 				target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePlaceRecipe(ILnet/minecraft/world/item/crafting/RecipeHolder;Z)V",
	//- 				shift = At.Shift.AFTER
	//- 		)
	//- 		//# else
	//- 		at = @At(
	//- 				value = "INVOKE",
	//- 				target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePlaceRecipe(ILnet/minecraft/world/item/crafting/Recipe;Z)V",
	//- 				shift = At.Shift.AFTER
	//- 		)
	//- 		//# end
	//- )
	//- //# if MC_VERSION_NUMBER >= 12109
	//- public void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
	//- 	int mouseButton = event.button();
	//- //# else
	//- public void mouseClicked(double x, double y, int mouseButton, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
	//- //# end
	//- 	//# if MC_VERSION_NUMBER >= 260300
	//- 	if (MouseWheelie.config.general.enableQuickCraft && mouseButton == InputConstants.MOUSE_BUTTON_RIGHT) {
	//- 	//# else
	//- 	if (MouseWheelie.config.general.enableQuickCraft && mouseButton == 1) {
	//- 	//# end
	//- 		quickCraftHandler.triggerQuickCraft();
	//- 	}
	//- }
	//# end

	//# if MC_VERSION_NUMBER >= 12109
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
	//# else
	//- @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	//- public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
	//# end
		if (MouseWheelie.config.general.enableQuickCraft && isVisible() && !minecraft.player.isSpectator()) {
			//# if MC_VERSION_NUMBER >= 12109
			if (Minecraft.getInstance().options.keyDrop.matches(event)) {
			//# else
			//- if (Minecraft.getInstance().options.keyDrop.matches(keyCode, scanCode)) {
			//# end
				ignoreTextInput = false;
				if (quickCraftHandler.triggerDropCraft()) {
					callbackInfoReturnable.setReturnValue(true);
				}
			}
		}
	}

	@Unique
	private int getLeft() {
		//# if MC_VERSION_NUMBER >= 260400
		return getX();
		//# elif MC_VERSION_NUMBER >= 12102
		//- return getXOrigin();
		//# else
		//- // we try to position by the search box, so we can keep compatible with other mods that move stuff around
		//- if (searchBox != null) {
		//- 	return this.searchBox.getX() - 25;
		//- } else {
		//- 	return (this.width - WIDGET_WIDTH) / 2 - this.xOffset;
		//- }
		//# end
	}

	@Unique
	private int getTop() {
		//# if MC_VERSION_NUMBER >= 260400
		return getY();
		//# elif MC_VERSION_NUMBER >= 12102
		//- return getYOrigin();
		//# else
		//- // we try to position by the search box, so we can keep compatible with other mods that move stuff around
		//- if (searchBox != null) {
		//- 	return this.searchBox.getY() - 13;
		//- } else {
		//- 	return (this.height - WIDGET_HEIGHT) / 2;
		//- }
		//# end
	}
}
