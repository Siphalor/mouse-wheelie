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

package de.siphalor.mousewheelie.client.mixin.entity;

import com.mojang.authlib.GameProfile;
//- import de.siphalor.mousewheelie.MouseWheelie;
//- import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//- import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
//- import net.minecraft.world.InteractionHand;
//- import net.minecraft.world.entity.item.ItemEntity;

@Mixin(LocalPlayer.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayer {
	public MixinClientPlayerEntity(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "clientSideCloseContainer", at = @At("HEAD"))
	public void onScreenClosed(CallbackInfo callbackInfo) {
		InteractionManager.clear();
	}

	//# if MC_VERSION_NUMBER < 260300
	//- @Inject(method = "drop", at = @At("HEAD"))
	//- public void onDropSelectedItem(boolean all, CallbackInfoReturnable<ItemEntity> callbackInfoReturnable) {
	//- 	if (MouseWheelie.config.refill.enable && MouseWheelie.config.refill.drop) {
	//- 		if (!getMainHandItem().isEmpty()) {
	//- 			SlotRefiller.scheduleRefillUnchecked(InteractionHand.MAIN_HAND, getInventory(), getMainHandItem().copy());
	//- 		}
	//- 	}
	//- }

	//- @Inject(method = "drop", at = @At("RETURN"))
	//- public void onSelectedItemDropped(boolean all, CallbackInfoReturnable<ItemEntity> callbackInfoReturnable) {
	//- 	SlotRefiller.performRefill();
	//- }
	//# end
}
