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
import net.minecraft.network.packet.s2c.play.ConfirmGuiActionS2CPacket;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HeldItemChangeS2CPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
	@Shadow
	private MinecraftClient client;

	@Inject(method = "onGuiActionConfirm", at = @At("RETURN"))
	public void onGuiActionConfirmed(ConfirmGuiActionS2CPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.GUI_CONFIRM);
	}

	@Inject(method = "onHeldItemChange", at = @At("HEAD"))
	public void onHeldItemChangeBegin(HeldItemChangeS2CPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.HELD_ITEM_CHANGE);
	}

	@Inject(method = "onContainerSlotUpdate", at = @At("RETURN"))
	public void onGuiSlotUpdateBegin(ContainerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		MWClient.lastUpdatedSlot = packet.getSlot();
		InteractionManager.triggerSend(InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE);
	}

	@Inject(method = "onContainerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/PlayerContainer;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdate(ContainerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		if (!MWClient.performRefill() && MWConfig.refill.other) {
			//noinspection ConstantConditions
			PlayerInventory inventory = client.player.inventory;
			if (packet.getItemStack().isEmpty() && MinecraftClient.getInstance().currentScreen == null) {
				if (packet.getSlot() - 36 == inventory.selectedSlot) { // MAIN_HAND
					ItemStack stack = inventory.getInvStack(inventory.selectedSlot);
					if (!stack.isEmpty()) {
						MWClient.scheduleRefill(Hand.MAIN_HAND, inventory, stack.copy());
					}
				} else if (packet.getSlot() == 40) { // OFF_HAND
					ItemStack stack = inventory.getInvStack(40);
					if (!stack.isEmpty()) {
						MWClient.scheduleRefill(Hand.OFF_HAND, inventory, stack.copy());
					}
				}
			}
		}
	}

	@Inject(method = "onContainerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/Container;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
	public void onGuiSlotUpdated(ContainerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		MWClient.performRefill();
	}
}
