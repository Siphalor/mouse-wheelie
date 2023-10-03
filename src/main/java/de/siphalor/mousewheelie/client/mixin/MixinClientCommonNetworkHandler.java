package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.network.MWClientNetworking;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class MixinClientCommonNetworkHandler {

	@Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
	public void onSend(Packet<?> packet, CallbackInfo callbackInfo) {
		if (packet instanceof PlayerActionC2SPacket) {
			if (((PlayerActionC2SPacket) packet).getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
				MWClientNetworking.blockNextGuiUpdateRefillTriggers(2);
			}
		}
	}
}
