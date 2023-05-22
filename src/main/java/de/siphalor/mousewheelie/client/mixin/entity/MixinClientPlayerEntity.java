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

package de.siphalor.mousewheelie.client.mixin.entity;

import com.mojang.authlib.GameProfile;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
	public MixinClientPlayerEntity(ClientWorld clientWorld_1, GameProfile gameProfile_1) {
		super(clientWorld_1, gameProfile_1);
	}

	@Inject(method = "closeScreen", at = @At("HEAD"))
	public void onScreenClosed(CallbackInfo callbackInfo) {
		InteractionManager.clear();
	}

	@Inject(method = "dropSelectedItem", at = @At("HEAD"))
	public void onDropSelectedItem(boolean all, CallbackInfoReturnable<ItemEntity> callbackInfoReturnable) {
		if (MWConfig.refill.enable && MWConfig.refill.drop) {
			if (!getMainHandStack().isEmpty()) {
				MWClient.scheduleRefill(Hand.MAIN_HAND, inventory, getMainHandStack().copy());
			}
		}
	}

	@Inject(method = "dropSelectedItem", at = @At("RETURN"))
	public void onSelectedItemDropped(boolean all, CallbackInfoReturnable<ItemEntity> callbackInfoReturnable) {
		MWClient.performRefill();
	}
}
