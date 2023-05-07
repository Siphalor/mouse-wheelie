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

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.compat.FabricCreativeGuiHelper;
import de.siphalor.mousewheelie.client.inventory.CreativeContainerScreenHelper;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISpecialScrollableScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> implements ISpecialScrollableScreen, IContainerScreen {

	@Shadow
	private static ItemGroup selectedTab;

	@Shadow
	protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	@Shadow
	protected abstract void onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType);

	@Shadow public abstract boolean isInventoryTabSelected();

	public MixinCreativeInventoryScreen(CreativeInventoryScreen.CreativeScreenHandler container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		if (MWConfig.scrolling.scrollCreativeMenuTabs) {
			double relMouseY = mouseY - this.y;
			double relMouseX = mouseX - this.x;
			boolean yOverTopTabs = (-32 <= relMouseY && relMouseY <= 0);
			boolean yOverBottomTabs = (this.backgroundHeight <= relMouseY && relMouseY <= this.backgroundHeight + 32);
			boolean overTabs = (0 <= relMouseX && relMouseX <= this.backgroundWidth) && (yOverTopTabs || yOverBottomTabs);

			if (overTabs) {
				List<ItemGroup> groupsToDisplay = ItemGroups.getGroupsToDisplay();
				int selectedTabIndex = groupsToDisplay.indexOf(selectedTab);
				if (selectedTabIndex < 0) {
					return ScrollAction.FAILURE;
				}
				if (FabricLoader.getInstance().isModLoaded("fabric-item-group-api-v1")) {
					FabricCreativeGuiHelper helper = new FabricCreativeGuiHelper((CreativeInventoryScreen) (Object) this);
					int newIndex = MathHelper.clamp(selectedTabIndex + (int) Math.round(scrollAmount), 0, groupsToDisplay.size() - 1);
					int newPage = helper.getPageForTabIndex(newIndex);
					if (newPage < helper.getCurrentPage())
						helper.previousPage();
					if (newPage > helper.getCurrentPage())
						helper.nextPage();
					setSelectedTab(groupsToDisplay.get(newIndex));
				} else {
					setSelectedTab(groupsToDisplay.get(MathHelper.clamp((int) (selectedTabIndex + Math.round(scrollAmount)), 0, groupsToDisplay.size() - 1)));
				}
				return ScrollAction.SUCCESS;
			}
		}

		if (MWConfig.scrolling.enable && !isInventoryTabSelected()) {
			if (MWConfig.scrolling.scrollCreativeMenuItems == hasAltDown())
				return ScrollAction.ABORT;
			Slot hoverSlot = this.mouseWheelie_getSlotAt(mouseX, mouseY);
			if (hoverSlot != null) {
				new CreativeContainerScreenHelper<>((CreativeInventoryScreen) (Object) this, (slot, data, slotActionType) ->
						new InteractionManager.CallbackEvent(() -> {
							onMouseClick(slot, slot.id, data, slotActionType);
							return InteractionManager.TICK_WAITER;
						})
				).scroll(hoverSlot, scrollAmount < 0);
				return ScrollAction.SUCCESS;
			}
		}

		return ScrollAction.PASS;
	}
}
