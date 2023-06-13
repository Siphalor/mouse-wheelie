package de.siphalor.mousewheelie.common.network;

import lombok.CustomLog;
import lombok.Value;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
@CustomLog
public class ReorderInventoryPacket {
	int syncId;
	int[] slotMappings;

	public void write(@NotNull PacketByteBuf buf) {
		buf.writeVarInt(syncId);
		buf.writeIntArray(slotMappings);
	}

	public static @Nullable ReorderInventoryPacket read(PacketByteBuf buf) {
		int syncId = buf.readVarInt();
		int[] reorderedIndices = buf.readIntArray();

		if (reorderedIndices.length % 2 != 0) {
			log.warn("Received reorder inventory packet with invalid data!");
			return null;
		}

		return new ReorderInventoryPacket(syncId, reorderedIndices);
	}
}
