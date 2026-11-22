/*
 * Copyright 2026 Siphalor and contributors
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

package de.siphalor.mousewheelie.client.recipe_book;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.mixin.StackedItemContentsAccessor;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
//- import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
//- import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
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

@RequiredArgsConstructor
@CustomLog
public class RecipeBookQuickCraftHandler {
	private final Minecraft minecraft = Minecraft.getInstance();
	private final Predicate<Slot> isCraftingSlot;
	private final Supplier<ScreenRectangle> componentBoundsSupplier;
	private final Runnable updateRecipes;
	//# if MC_VERSION_NUMBER >= 12103
	private final Supplier<Pair<RecipeCollection, RecipeDisplayId>> getLastClickedCollectionAndRecipe;
	//# elif MC_VERSION_NUMBER >= 12002
	//- private final Supplier<Pair<RecipeCollection, RecipeHolder<?>>> getLastClickedCollectionAndRecipe;
	//# else
	//- private final Supplier<Pair<RecipeCollection, Recipe<?>>> getLastClickedCollectionAndRecipe;
	//# end
	private final RecipeBookMenu menu;
	private final RecipeBookPage recipeBookPage;
	//# if MC_VERSION_NUMBER >= 12103
	private final StackedItemContents stackedContents;
	//# else
	//- private final StackedContents stackedContents;
	//# end

	private int resultSlotIndex = Integer.MAX_VALUE;

	public void triggerQuickCraft() {
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
							&& !isCraftingSlot.test(menu.slots.get(MWClient.lastUpdatedSlot))
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

	public boolean triggerDropCraft() {
		//# if MC_VERSION_NUMBER >= 12103
		RecipeDisplayEntry oldRecipeEntry = getLastClickedRecipeEntry();
		//# elif MC_VERSION_NUMBER >= 12002
		//- RecipeHolder<?> oldRecipeEntry = recipeBookPage.getLastClickedRecipe();
		//# else
		//- Recipe<?> oldRecipeEntry = recipeBookPage.getLastClickedRecipe();
		//# end
		//# if MC_VERSION_NUMBER < 260400
		//- ScreenRectangle bounds = componentBoundsSupplier.get();
		//# end
		if (this.recipeBookPage.mouseClicked(
				//# if MC_VERSION_NUMBER >= 12109
				new MouseButtonEvent(MWClient.getMouseX(), MWClient.getMouseY(), new MouseButtonInfo(
						//# if MC_VERSION_NUMBER >= 260300
						InputConstants.MOUSE_BUTTON_LEFT, 0
						//# else
						//- 0, 0
						//# end
				)),
				//# else
				//- MWClient.getMouseX(),
				//- MWClient.getMouseY(),
				//- 0,
				//# end
				//# if MC_VERSION_NUMBER < 260400
				//- bounds.position().x(),
				//- bounds.position().y(),
				//- bounds.width(),
				//- bounds.height()
				//- /*# if MC_VERSION_NUMBER >= 12109 */,/*# end */
				//# end
				//# if MC_VERSION_NUMBER >= 12109
				false
				//# end
		)) {
			RecipeCollection resultCollection = getLastClickedCollectionAndRecipe.get().getLeft();
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
				return false;
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
							(triggerType) -> !isCraftingSlot.test(menu.slots.get(MWClient.lastUpdatedSlot))
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
							new ServerboundPlaceRecipePacket(menu.containerId, recipeEntry.id(), false),
							//# else
							//- new ServerboundPlaceRecipePacket(menu.containerId, recipeEntry, false),
							//# end
							(triggerType) -> {
								if (MWClient.lastUpdatedSlot < 0 || MWClient.lastUpdatedSlot >= menu.slots.size()) {
									return false;
								}
								return !isCraftingSlot.test(menu.slots.get(MWClient.lastUpdatedSlot));
							}
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
				this.updateRecipes.run();
				return InteractionManager.TICK_WAITER;
			}, true));
			return true;
		}
		return false;
	}

	//# if MC_VERSION_NUMBER >= 12103
	@Unique
	private @Nullable RecipeDisplayEntry getLastClickedRecipeEntry() {
		var lastClickedCollectionAndRecipe = getLastClickedCollectionAndRecipe.get();
		RecipeCollection recipeCollection = lastClickedCollectionAndRecipe.getLeft();
		RecipeDisplayId recipeId = lastClickedCollectionAndRecipe.getRight();
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

	private int getBiggestCraftingStackSize() {
		int resSlot = getResultSlotIndex();
		int cnt = 0;
		for (int i = 0; i < menu.slots.size(); i++) {
			if (i == resSlot || !isCraftingSlot.test(menu.slots.get(i))) continue;
			cnt = Math.max(cnt, menu.slots.get(i).getItem().getCount());
		}
		return cnt;
	}

	private int getResultSlotIndex() {
		if (resultSlotIndex != Integer.MAX_VALUE) {
			return resultSlotIndex;
		}

		//# if MC_VERSION_NUMBER >= 12103
		if (menu instanceof AbstractCraftingMenu) {
			return resultSlotIndex =
					((ISlot)((AbstractCraftingMenu) menu).getResultSlot())
							.mouseWheelie_getIdInContainer();
		}

		Slot candidate = null;
		for (Slot slot : menu.slots) {
			if (!isCraftingSlot.test(slot) || slot.mayPlace(ItemStack.EMPTY)) {
				continue;
			}
			candidate = slot;
		}

		if (candidate == null) {
			log.warn("Failed to find result slot of recipe book");
			return resultSlotIndex = -1;
		}

		return resultSlotIndex = ((ISlot) candidate).mouseWheelie_getIdInContainer();
		//# else
		//- return resultSlotIndex = menu.getResultSlotIndex();
		//# end
	}
}
