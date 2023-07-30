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
