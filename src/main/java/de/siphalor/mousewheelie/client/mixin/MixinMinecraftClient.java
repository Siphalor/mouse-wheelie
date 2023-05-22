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

package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
	@Shadow
	public ClientPlayerEntity player;
	private ItemStack mouseWheelie_mainHandStack;
	private ItemStack mouseWheelie_offHandStack;

	@Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"))
	public void onItemUse(CallbackInfo callbackInfo) {
		if (MWConfig.refill.enable && MWConfig.refill.use) {
			mouseWheelie_mainHandStack = player.getMainHandStack();
			mouseWheelie_mainHandStack = mouseWheelie_mainHandStack.isEmpty() ? null : mouseWheelie_mainHandStack.copy();
			mouseWheelie_offHandStack = player.getOffHandStack();
			mouseWheelie_offHandStack = mouseWheelie_offHandStack.isEmpty() ? null : mouseWheelie_offHandStack.copy();
		}
	}

	@Inject(method = "doItemUse", at = @At("RETURN"))
	public void onItemUsed(CallbackInfo callbackInfo) {
		if (mouseWheelie_mainHandStack != null && player.getMainHandStack().isEmpty()) {
			MWClient.scheduleRefill(Hand.MAIN_HAND, player.inventory, mouseWheelie_mainHandStack);
		} else if (mouseWheelie_offHandStack != null && player.getOffHandStack().isEmpty()) {
			MWClient.scheduleRefill(Hand.OFF_HAND, player.inventory, mouseWheelie_offHandStack);
		}
		MWClient.performRefill();
		mouseWheelie_mainHandStack = null;
		mouseWheelie_offHandStack = null;
	}
}
