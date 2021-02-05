/*
 * Copyright 2021 Siphalor
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

import de.siphalor.mousewheelie.client.util.accessors.IMerchantScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantScreen.class)
public abstract class MixinMerchantScreen implements IMerchantScreen {

	@Shadow
	private int field_19161;

	@Shadow
	protected abstract void syncRecipeIndex();

	@Shadow
	private int field_19163;

	@Override
	public void mouseWheelie_setRecipeId(int id) {
		field_19161 = id;
	}

	@Override
	public void mouseWheelie_syncRecipeId() {
		syncRecipeIndex();
	}

	@Override
	public int getRecipeIdOffset() {
		return field_19163;
	}
}
