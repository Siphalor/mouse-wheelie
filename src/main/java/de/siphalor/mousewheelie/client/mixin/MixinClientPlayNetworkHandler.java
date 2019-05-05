package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.Core;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.ConfirmGuiActionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
	@Inject(method = "onGuiActionConfirm", at = @At("RETURN"))
	public void onGuiActionConfirmed(ConfirmGuiActionS2CPacket packet, CallbackInfo callbackInfo) {
		if(Core.sending)
			Core.triggerSend();
	}
}
