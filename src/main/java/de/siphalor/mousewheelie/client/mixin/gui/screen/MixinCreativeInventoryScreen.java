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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
//- import de.siphalor.mousewheelie.client.compat.FabricCreativeGuiHelper;
import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IContainerScreen;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import de.siphalor.mousewheelie.client.util.inject.ISpecialScrollableScreen;
import java.util.List;
//- import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
//- import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
//- import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements ISpecialScrollableScreen, IContainerScreen {

	@Shadow
	private static CreativeModeTab selectedTab;

	@Shadow
	protected abstract void selectTab(CreativeModeTab itemGroup_1);

	@Shadow
	protected abstract void slotClicked(
			//# if MC_VERSION_NUMBER >= 260400
			SlotWidget slotWidget,
			//# else
			//- Slot slot,
			//# end
			int invSlot,
			int button,
			//# if MC_VERSION_NUMBER >= 260100
			ContainerInput slotActionType
			//# else
			//- ClickType slotActionType
			//# end
	);

	@Shadow public abstract boolean isInventoryOpen();

	public MixinCreativeInventoryScreen(CreativeModeInventoryScreen.ItemPickerMenu container_1, Inventory playerInventory_1, Component textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		if (MouseWheelie.config.scrolling.scrollCreativeMenuTabs) {
			double relMouseY = mouseY - this.topPos;
			double relMouseX = mouseX - this.leftPos;
			boolean yOverTopTabs = (-32 <= relMouseY && relMouseY <= 0);
			boolean yOverBottomTabs = (this.imageHeight <= relMouseY && relMouseY <= this.imageHeight + 32);
			boolean overTabs = (0 <= relMouseX && relMouseX <= this.imageWidth) && (yOverTopTabs || yOverBottomTabs);

			if (overTabs) {
				List<CreativeModeTab> groupsToDisplay = CreativeModeTabs.tabs();
				int selectedTabIndex = groupsToDisplay.indexOf(selectedTab);
				if (selectedTabIndex < 0) {
					return ScrollAction.FAILURE;
				}
				//# if MC_VERSION_NUMBER < 12006
				//- if (FabricLoader.getInstance().isModLoaded("fabric-item-group-api-v1")) {
				//- 	FabricCreativeGuiHelper helper = new FabricCreativeGuiHelper((CreativeModeInventoryScreen) (Object) this);
				//- 	int newIndex = Mth.clamp(
				//- 			selectedTabIndex + (int) Math.signum(scrollAmount),
				//- 			0,
				//- 			groupsToDisplay.size() - 1
				//- 	);
				//- 	int newPage = helper.getPageForTabIndex(newIndex);
				//- 	if (newPage < helper.getCurrentPage())
				//- 		helper.previousPage();
				//- 	if (newPage > helper.getCurrentPage())
				//- 		helper.nextPage();
				//- 	selectTab(groupsToDisplay.get(newIndex));
				//- 	return ScrollAction.SUCCESS;
				//- }
				//# end
				selectTab(groupsToDisplay.get(
						//# if MC_VERSION_NUMBER >= 260400
						Math.clamp(
						//# else
						//- Mth.clamp(
						//# end
								(int) (selectedTabIndex + Math.signum(scrollAmount)),
								0,
								groupsToDisplay.size() - 1)
						)
				);
				return ScrollAction.SUCCESS;
			}
		}

		if (MouseWheelie.config.scrolling.enable && !isInventoryOpen()) {
			if (MouseWheelie.config.scrolling.scrollCreativeMenuItems == MWClient.isScrollModeToggled())
				return ScrollAction.ABORT;
			Slot hoverSlot = this.mouseWheelie_getSlotAt(mouseX, mouseY);
			if (hoverSlot != null) {
				ContainerScreenHelper.of(this, (slot, data, slotActionType) ->
						new InteractionManager.CallbackEvent(() -> {
							slotClicked(
									//# if MC_VERSION_NUMBER >= 260400
									new SlotWidget(slot, minecraft, width, leftPos, topPos),
									//# else
									//- slot,
									//# end
									((ISlot) slot).mouseWheelie_getIdInContainer(),
									data,
									slotActionType
							);
							return InteractionManager.TICK_WAITER;
						}, true)
				).scroll(hoverSlot, scrollAmount < 0);
				return ScrollAction.SUCCESS;
			}
		}

		return ScrollAction.PASS;
	}
}
