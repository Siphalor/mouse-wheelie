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
