/*
 * Copyright 2020-2022 Siphalor
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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.util.IRecipeBookWidget;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IScrollableRecipeBook;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.CraftingTableScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingTableScreen.class)
public abstract class MixinCraftingTableScreen extends ContainerScreen<CraftingTableContainer> implements IScrollableRecipeBook {
	@Shadow
	@Final
	private RecipeBookWidget recipeBookGui;

	public MixinCraftingTableScreen(CraftingTableContainer container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScrollRecipeBook(double mouseX, double mouseY, double scrollAmount) {
		return ((IRecipeBookWidget) recipeBookGui).mouseWheelie_scrollRecipeBook(mouseX, mouseY, scrollAmount);
	}
}
