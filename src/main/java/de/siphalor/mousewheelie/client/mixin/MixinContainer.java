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

package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.container.Container;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(Container.class)
public abstract class MixinContainer {
	@Shadow
	public abstract Slot getSlot(int index);

	@Inject(method = "updateSlotStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/Slot;setStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onSlotUpdate(List<ItemStack> itemStacks, CallbackInfo callbackInfo, int index) {
		//noinspection ConstantConditions
		if ((Object) this instanceof PlayerContainer && MWConfig.refill.other) {
			PlayerInventory inventory = MinecraftClient.getInstance().player.inventory;
			if (inventory.selectedSlot == ((ISlot) getSlot(index)).mouseWheelie_getInvSlot()) {
				ItemStack stack = inventory.getMainHandStack();
				if (!stack.isEmpty() && itemStacks.get(index).isEmpty()) {
					MWClient.scheduleRefill(Hand.MAIN_HAND, inventory, stack.copy());
				}
			} else if (40 == ((ISlot) getSlot(index)).mouseWheelie_getInvSlot()) {
				ItemStack stack = inventory.getInvStack(40);
				if (!stack.isEmpty() && itemStacks.get(index).isEmpty()) {
					MWClient.scheduleRefill(Hand.OFF_HAND, inventory, stack.copy());
				}
			}
		}
	}

	@Inject(method = "updateSlotStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/Slot;setStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onSlotUpdated(List<ItemStack> stacks, CallbackInfo callbackInfo, int index) {
		MWClient.performRefill();
	}

}
