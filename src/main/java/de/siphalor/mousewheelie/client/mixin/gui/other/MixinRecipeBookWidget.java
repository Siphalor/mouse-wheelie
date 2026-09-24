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

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.mixin.StackedItemContentsAccessor;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookResults;
import de.siphalor.mousewheelie.client.util.inject.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import java.util.List;
import lombok.CustomLog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//- import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
//- import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.util.Mth;
//- import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
//- import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
//- import net.minecraft.world.item.crafting.Recipe;
//- import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

@Environment(EnvType.CLIENT)
@Mixin(RecipeBookComponent.class)
@CustomLog
public abstract class MixinRecipeBookWidget implements IRecipeBookWidget {
	@Unique
	private static final int WIDGET_HEIGHT = RecipeBookComponent.IMAGE_HEIGHT;
	@Unique
	private static final int WIDGET_WIDTH = RecipeBookComponent.IMAGE_WIDTH;

	@Shadow @Final
	private RecipeBookPage recipeBookPage;

	@Shadow private int width;

	@Shadow
	private int height;

	@Shadow private int xOffset;

	@Shadow @Final private List<RecipeBookTabButton> tabButtons;

	@Shadow private RecipeBookTabButton selectedTab;

	@Shadow
	//# if MC_VERSION_NUMBER >= 12103
	protected abstract void updateCollections(boolean resetPageNumber, boolean isFiltering);

	@Shadow
	protected abstract boolean isFiltering();
	//# else
	//- protected abstract void updateCollections(boolean resetPageNumber);
	//# end

	@Shadow
	public abstract boolean isVisible();

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

	//# if MC_VERSION_NUMBER >= 12102
	@Shadow
	protected abstract int getXOrigin();

	@Shadow
	protected abstract int getYOrigin();
	//# end

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
			castedBookPage.mouseWheelie_setCurrentPage(Mth.clamp(
					(int) (castedBookPage.mouseWheelie_getCurrentPage() + Math.signum(scrollAmount)),
					0,
					Math.max(maxPage, 0)
			));
			castedBookPage.mouseWheelie_refreshResultButtons();
			return ScrollAction.SUCCESS;
		} else if(mouseX >= left - 30 && mouseX < left) {
			int index = tabButtons.indexOf(selectedTab);
			int inc = (int) Math.signum(scrollAmount);
			while (true) {
				index += inc;
				if (index < 0 || index >= tabButtons.size()) {
					return ScrollAction.PASS;
				}
				RecipeBookTabButton tab = tabButtons.get(index);
				if (tab.visible) {
					if (tab != selectedTab) {
						//# if MC_VERSION_NUMBER >= 12111
						selectedTab.unselect();
						selectedTab = tab;
						selectedTab.select();
						//# else
						//- selectedTab.setStateTriggered(false);
						//- selectedTab = tab;
						//- selectedTab.setStateTriggered(true);
						//# end
						//# if MC_VERSION_NUMBER >= 12103
						updateCollections(true, isFiltering());
						//# else
						//- updateCollections(true);
						//# end
					}
					break;
				}
			}
			return ScrollAction.SUCCESS;
		}
		return ScrollAction.PASS;
	}

	@Inject(
			method = "mouseClicked",
			//# if MC_VERSION_NUMBER >= 12103
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;isOffsetNextToMainGUI()Z"
			)
			//# elif MC_VERSION_NUMBER >= 12002
			//- at = @At(
			//- 		value = "INVOKE",
			//- 		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePlaceRecipe(ILnet/minecraft/world/item/crafting/RecipeHolder;Z)V",
			//- 		shift = At.Shift.AFTER
			//- )
			//# else
			//- at = @At(
			//- 		value = "INVOKE",
			//- 		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePlaceRecipe(ILnet/minecraft/world/item/crafting/Recipe;Z)V",
			//- 		shift = At.Shift.AFTER
			//- )
			//# end
	)
	//# if MC_VERSION_NUMBER >= 12109
	public void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		int mouseButton = event.button();
	//# else
	//- public void mouseClicked(double x, double y, int mouseButton, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
	//# end
		//# if MC_VERSION_NUMBER >= 260300
		if (MouseWheelie.config.general.enableQuickCraft && mouseButton == InputConstants.MOUSE_BUTTON_RIGHT) {
		//# else
		//- if (MouseWheelie.config.general.enableQuickCraft && mouseButton == 1) {
		//# end
			int resSlot = getResultSlotIndex();
			//# if MC_VERSION_NUMBER >= 12103
			RecipeDisplayEntry recipeEntry = getLastClickedRecipeEntry();
			//# elif MC_VERSION_NUMBER >= 12002
			//- RecipeHolder<?> recipeEntry = recipeBookPage.getLastClickedRecipe();
			//# else
			//- Recipe<?> recipeEntry = recipeBookPage.getLastClickedRecipe();
			//# end
			if (recipeEntry != null && canCraftMore(recipeEntry)) {
				InteractionManager.clear();
				InteractionManager.setWaiter((InteractionManager.TriggerType triggerType) ->
						MWClient.lastUpdatedSlot != -1
								&& !isCraftingSlot(menu.slots.get(MWClient.lastUpdatedSlot))
				);
			}
			InteractionManager.pushClickEvent(
					menu.containerId,
					resSlot,
					0,
					//# if MC_VERSION_NUMBER >= 260100
					MWClient.WHOLE_STACK_MODIFIER.isDown() ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP
					//# else
					//- MWClient.WHOLE_STACK_MODIFIER.isDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP
					//# end
			);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	//# if MC_VERSION_NUMBER >= 12109
	public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
	//# else
	//- public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
	//# end
		if (MouseWheelie.config.general.enableQuickCraft && isVisible() && !minecraft.player.isSpectator()) {
			//# if MC_VERSION_NUMBER >= 12109
			if (Minecraft.getInstance().options.keyDrop.matches(event)) {
			//# else
			//- if (Minecraft.getInstance().options.keyDrop.matches(keyCode, scanCode)) {
			//# end
				ignoreTextInput = false;
				//# if MC_VERSION_NUMBER >= 12103
				RecipeDisplayEntry oldRecipeEntry = getLastClickedRecipeEntry();
				//# elif MC_VERSION_NUMBER >= 12002
				//- RecipeHolder<?> oldRecipeEntry = recipeBookPage.getLastClickedRecipe();
				//# else
				//- Recipe<?> oldRecipeEntry = recipeBookPage.getLastClickedRecipe();
				//# end
				if (this.recipeBookPage.mouseClicked(
						//# if MC_VERSION_NUMBER >= 12109
						new MouseButtonEvent(MWClient.getMouseX(), MWClient.getMouseY(), new MouseButtonInfo(0, 0)),
						//# else
						//- MWClient.getMouseX(),
						//- MWClient.getMouseY(),
						//- 0,
						//# end
						getLeft(),
						getTop(),
						147, 166 // width, height
						//# if MC_VERSION_NUMBER >= 12109
						, false
						//# end
				)) {
					RecipeCollection resultCollection = recipeBookPage.getLastClickedRecipeCollection();
					//# if MC_VERSION_NUMBER >= 12103
					RecipeDisplayEntry recipeEntry = getLastClickedRecipeEntry();
					if (!resultCollection.isCraftable(recipeEntry.id())) {
					//# elif MC_VERSION_NUMBER >= 12002
					//- RecipeHolder<?> recipeEntry = recipeBookPage.getLastClickedRecipe();
					//- if (!resultCollection.isCraftable(recipeEntry)) {
					//# else
					//- Recipe<?> recipeEntry = recipeBookPage.getLastClickedRecipe();
					//- if (!resultCollection.isCraftable(recipeEntry)) {
					//# end
						return;
					}
					int resSlot = getResultSlotIndex();
					if (MWClient.ALL_OF_KIND_MODIFIER.isDown()) {
						if (oldRecipeEntry != recipeEntry || menu.slots.get(resSlot).getItem().isEmpty() || canCraftMore(recipeEntry)) {
							InteractionManager.push(new InteractionManager.PacketEvent(
									//# if MC_VERSION_NUMBER >= 12103
									new ServerboundPlaceRecipePacket(menu.containerId, recipeEntry.id(), true),
									//# else
									//- new ServerboundPlaceRecipePacket(menu.containerId, recipeEntry, true),
									//# end
									(triggerType) -> !isCraftingSlot(menu.slots.get(MWClient.lastUpdatedSlot))
							));
						}
						int cnt = getMaxCraftsCount(recipeEntry);
						for (int i = 1; i < cnt; i++) {
							//# if MC_VERSION_NUMBER >= 260100
							InteractionManager.pushClickEvent(menu.containerId, resSlot, 1, ContainerInput.THROW);
							//# else
							//- InteractionManager.pushClickEvent(menu.containerId, resSlot, 1, ClickType.THROW);
							//# end
						}
					} else {
						if (oldRecipeEntry != recipeEntry || menu.slots.get(resSlot).getItem().isEmpty()) {
							InteractionManager.push(new InteractionManager.PacketEvent(
									//# if MC_VERSION_NUMBER >= 12103
									new ServerboundPlaceRecipePacket(menu.containerId, recipeEntry.id(), true),
									//# else
									//- new ServerboundPlaceRecipePacket(menu.containerId, recipeEntry, true),
									//# end
									(triggerType) -> !isCraftingSlot(menu.slots.get(MWClient.lastUpdatedSlot))
							));
						}
					}
					InteractionManager.push(new InteractionManager.CallbackEvent(() -> {
						//# if MC_VERSION_NUMBER >= 260100
						minecraft.gameMode.handleContainerInput(
								menu.containerId,
								getResultSlotIndex(),
								0,
								ContainerInput.THROW,
								minecraft.player
						);
						//# else
						//- minecraft.gameMode.handleInventoryMouseClick(
						//- 		menu.containerId,
						//- 		getResultSlotIndex(),
						//- 		0,
						//- 		ClickType.THROW,
						//- 		minecraft.player
						//- );
						//# end
						//# if MC_VERSION_NUMBER >= 12103
						updateCollections(false, isFiltering());
						//# else
						//- updateCollections(false);
						//# end
						return InteractionManager.TICK_WAITER;
					}, true));
					callbackInfoReturnable.setReturnValue(true);
				}
			}
		}
	}

	//# if MC_VERSION_NUMBER >= 12103
	@Unique
	private @Nullable RecipeDisplayEntry getLastClickedRecipeEntry() {
		RecipeCollection recipeCollection = recipeBookPage.getLastClickedRecipeCollection();
		RecipeDisplayId recipeId = recipeBookPage.getLastClickedRecipe();
		if (recipeCollection == null || recipeId == null) return null;

		for (RecipeDisplayEntry recipe : recipeCollection.getRecipes()) {
			if (recipe.id().equals(recipeId)) return recipe;
		}
		return null;
	}

	@Unique
	private boolean canCraftMore(RecipeDisplayEntry recipeEntry) {
		if (recipeEntry.craftingRequirements().isEmpty()) return false;
		int biggestCraftingStackSize = getBiggestCraftingStackSize();
		int maxCraftsCount = ((StackedItemContentsAccessor) stackedContents).getRaw().tryPickAll(
				//# if MC_VERSION_NUMBER >= 12104
				PlacementInfo.create(recipeEntry.craftingRequirements().get()).ingredients(),
				//# else
				//- recipeEntry.craftingRequirements().get().stream()
				//- 		.map(PlacementInfo::ingredientToContents)
				//- 		.toList(),
				//# end
				biggestCraftingStackSize + 1,
				null
		);
		return maxCraftsCount > biggestCraftingStackSize;
	}

	@Unique
	private int getMaxCraftsCount(RecipeDisplayEntry recipeEntry) {
		if (recipeEntry.craftingRequirements().isEmpty()) return 0;
		List<ItemStack> results = recipeEntry.resultItems(SlotDisplayContext.fromLevel(minecraft.level));
		if (results.isEmpty()) return 0;
		return ((StackedItemContentsAccessor) stackedContents).getRaw().tryPickAll(
				//# if MC_VERSION_NUMBER >= 12104
				PlacementInfo.create(recipeEntry.craftingRequirements().get()).ingredients(),
				//# else
				//- recipeEntry.craftingRequirements().get().stream()
				//- .map(PlacementInfo::ingredientToContents)
				//- .toList(),
				//# end
				ItemStackUtils.getMaxStackSize(results.get(0)),
				null
		);
	}
	//# elif MC_VERSION_NUMBER >= 12002
	//- @Unique
	//- private boolean canCraftMore(RecipeHolder<?> recipeEntry) {
	//- 	return getBiggestCraftingStackSize() < getMaxCraftsCount(recipeEntry);
	//- }

	//- @Unique
	//- private int getMaxCraftsCount(RecipeHolder<?> recipeEntry) {
	//- 	return stackedContents.getBiggestCraftableStack(
	//- 			recipeEntry,
	//- 			recipeEntry.value().getResultItem(minecraft.level.registryAccess()).getMaxStackSize(),
	//- 			null
	//- 	);
	//- }
	//# else
	//- @Unique
	//- private boolean canCraftMore(Recipe<?> recipeEntry) {
	//- 	return getBiggestCraftingStackSize() < getMaxCraftsCount(recipeEntry);
	//- }

	//- @Unique
	//- private int getMaxCraftsCount(Recipe<?> recipeEntry) {
	//- 	return stackedContents.getBiggestCraftableStack(
	//- 			recipeEntry,
	//- 			recipeEntry.getResultItem(minecraft.level.registryAccess()).getMaxStackSize(),
	//- 			null
	//- 	);
	//- }
	//# end

	@Unique
	private int getBiggestCraftingStackSize() {
		int resSlot = getResultSlotIndex();
		int cnt = 0;
		for (int i = 0; i < menu.slots.size(); i++) {
			if (i == resSlot || !isCraftingSlot(menu.slots.get(i))) continue;
			cnt = Math.max(cnt, menu.slots.get(i).getItem().getCount());
		}
		return cnt;
	}

	//# if MC_VERSION_NUMBER >= 12103
	@Unique
	private int resultSlotIndex = Integer.MAX_VALUE;
	@Unique
	private int getResultSlotIndex() {
		if (resultSlotIndex != Integer.MAX_VALUE) {
			return resultSlotIndex;
		}

		Slot candidate = null;
		for (Slot slot : menu.slots) {
			if (!isCraftingSlot(slot) || slot.mayPlace(ItemStack.EMPTY)) {
				continue;
			}
			candidate = slot;
		}

		if (candidate == null) {
			log.warn("Failed to find result slot of recipe book");
			return resultSlotIndex = -1;
		}
		return ((ISlot) candidate).mouseWheelie_getIdInContainer();
	}
	//# else
	//- @Unique
	//- private int getResultSlotIndex() {
	//- 	return menu.getResultSlotIndex();
	//- }
	//# end

	@Unique
	private int getLeft() {
		//# if MC_VERSION_NUMBER >= 12102
		return getXOrigin();
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
		//# if MC_VERSION_NUMBER >= 12102
		return getYOrigin();
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
