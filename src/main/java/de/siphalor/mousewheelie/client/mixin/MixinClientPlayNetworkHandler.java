package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ConfirmGuiActionS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
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

	private boolean mouseWheelie_scheduleRefill = false;

	@Inject(method = "onGuiActionConfirm", at = @At("RETURN"))
	public void onGuiActionConfirmed(ConfirmGuiActionS2CPacket packet, CallbackInfo callbackInfo) {
		InteractionManager.triggerSend(InteractionManager.TriggerType.GUI_CONFIRM);
	}

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At("RETURN"))
	public void onGuiSlotUpdateBegin(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		MWClient.lastUpdatedSlot = packet.getSlot();
		InteractionManager.triggerSend(InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE);
	}

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		if (MWClient.awaitSlotUpdate) {
			MWClient.awaitSlotUpdate = false;
			SlotRefiller.refill();
		} else {
			PlayerInventory inventory = client.player.inventory;
			if (packet.getItemStack().isEmpty() && packet.getSlot() - 36 == inventory.selectedSlot && MinecraftClient.getInstance().currentScreen == null) {
				ItemStack stack = inventory.getStack(inventory.selectedSlot);
				if (!stack.isEmpty()) {
					mouseWheelie_scheduleRefill = true;
					SlotRefiller.set(inventory, stack.copy());
				}
			}
		}
	}

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
	public void onGuiSlotUpdated(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		if (mouseWheelie_scheduleRefill) {
			if (MouseWheelie.CONFIG.refill.other)
				SlotRefiller.refill();
			mouseWheelie_scheduleRefill = false;
		}
	}
}
