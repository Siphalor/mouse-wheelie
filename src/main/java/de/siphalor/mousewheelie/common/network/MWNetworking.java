package de.siphalor.mousewheelie.common.network;

import de.siphalor.mousewheelie.MouseWheelie;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MWNetworking {
	protected MWNetworking() {}

	protected static final Identifier REORDER_INVENTORY_C2S_PACKET = new Identifier(MouseWheelie.MOD_ID, "reorder_inventory_c2s");

	protected static PacketByteBuf createBuffer() {
		return new PacketByteBuf(Unpooled.buffer());
	}
}
