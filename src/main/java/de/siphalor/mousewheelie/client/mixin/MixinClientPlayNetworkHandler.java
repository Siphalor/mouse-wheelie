package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.ClientCore;
import de.siphalor.mousewheelie.client.Config;
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
		InteractionManager.triggerSend();
	}

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE))
	public void onGuiSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo callbackInfo) {
		if (ClientCore.awaitSlotUpdate) {
			ClientCore.awaitSlotUpdate = false;
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
			if (Config.otherRefill.value)
				SlotRefiller.refill();
			mouseWheelie_scheduleRefill = false;
		}
	}
}
