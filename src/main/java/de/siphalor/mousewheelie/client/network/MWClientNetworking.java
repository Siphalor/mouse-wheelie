package de.siphalor.mousewheelie.client.network;

import de.siphalor.mousewheelie.common.network.MWNetworking;
import de.siphalor.mousewheelie.common.network.ReorderInventoryPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

public class MWClientNetworking extends MWNetworking {

	public static boolean canSendReorderPacket() {
		return ClientPlayNetworking.canSend(REORDER_INVENTORY_C2S_PACKET);
	}

	public static void send(ReorderInventoryPacket packet) {
		PacketByteBuf buffer = createBuffer();
		packet.write(buffer);
		ClientPlayNetworking.send(REORDER_INVENTORY_C2S_PACKET, buffer);
	}
}
