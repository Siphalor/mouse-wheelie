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
import de.siphalor.mousewheelie.client.network.InteractionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import net.minecraft.network.packet.s2c.play.HeldItemChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
	@Shadow
	private MinecraftClient client;

	@Unique
	private int blockedRefills;

	@Inject(method = "onConfirmScreenAction", at = @At("RETURN"))
	public void onGuiActionConfirmed(ConfirmScreenActionS2CPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.GUI_CONFIRM);
	}

	@Inject(method = "onHeldItemChange", at = @At("HEAD"))
	public void onHeldItemChangeBegin(HeldItemChangeS2CPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.HELD_ITEM_CHANGE);
	}

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At("RETURN"))
	public void onGuiSlotUpdateBegin(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		MWClient.lastUpdatedSlot = packet.getSlot();
		InteractionManager.triggerSend(InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE);
	}

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdateHotbar(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		if (MWConfig.refill.enable && MWConfig.refill.other) {
			//noinspection ConstantConditions
			PlayerInventory inventory = client.player.inventory;
			if (packet.getItemStack().isEmpty() && MinecraftClient.getInstance().currentScreen == null) {
				if (packet.getSlot() - 36 == inventory.selectedSlot) { // MAIN_HAND
					ItemStack stack = inventory.getStack(inventory.selectedSlot);
					if (!stack.isEmpty()) {
						MWClient.scheduleRefill(Hand.MAIN_HAND, inventory, stack.copy());
					}
				}
			}
		}
	}

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdateOther(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		//noinspection ConstantConditions
		if (MWConfig.refill.enable && MWConfig.refill.other && client.player.currentScreenHandler == client.player.playerScreenHandler && packet.getSlot() == 45) {
			PlayerInventory inventory = client.player.inventory;
			if (packet.getItemStack().isEmpty() && MinecraftClient.getInstance().currentScreen == null) {
				if (packet.getSlot() == 45) {
					ItemStack stack = inventory.offHand.get(0);
					if (!stack.isEmpty()) {
						MWClient.scheduleRefill(Hand.OFF_HAND, inventory, stack.copy());
					}
				}
			}
		}
	}

	@Inject(method = "onScreenHandlerSlotUpdate", require = 2,
			at = {
				@At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER),
				@At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER),
			}
	)
	public void onGuiSlotUpdated(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		if (packet.getSyncId() == 0) {
			if (blockedRefills > 0) {
				blockedRefills--;
				return;
			}

			MWClient.performRefill();
		}
	}

	@Inject(method = "sendPacket", at = @At("HEAD"))
	public void onSend(Packet<?> packet, CallbackInfo callbackInfo) {
		if (packet instanceof PlayerActionC2SPacket) {
			if (((PlayerActionC2SPacket) packet).getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
				blockedRefills = 2;
			}
		}
	}
}
