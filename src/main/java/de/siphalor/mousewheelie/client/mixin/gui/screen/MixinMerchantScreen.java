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

import de.siphalor.mousewheelie.client.util.inject.IMerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;

@Mixin(MerchantScreen.class)
public abstract class MixinMerchantScreen extends AbstractContainerScreen<MerchantMenu> implements IMerchantScreen {
	public MixinMerchantScreen(
			MerchantMenu menu,
			Inventory inventory,
			Component title
	) {
		super(menu, inventory, title);
	}

	//# if MC_VERSION_NUMBER >= 260400
	@Shadow
	protected abstract void selectTrade(int index);

	@Override
	public void mouseWheelie_setOfferAndSync(MerchantOffer offer) {
		int index = menu.getOffers().indexOf(offer);
		if (index >= 0) {
			selectTrade(index);
		}
	}
	//# else
	//- @Shadow private int shopItem;

	//- @Shadow protected abstract void postButtonClick();

	//- @Shadow
	//- int scrollOff;

	//- @Override
	//- public void mouseWheelie_setRecipeIdAndSync(int id) {
	//- 	shopItem = id + scrollOff;
	//- 	postButtonClick();
	//- }
	//# end
}
