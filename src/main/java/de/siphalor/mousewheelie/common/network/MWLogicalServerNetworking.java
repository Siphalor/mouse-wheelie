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
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.CustomLog;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that handles functionality on the logical server side.
 */
@CustomLog
public class MWLogicalServerNetworking extends MWNetworking {

	private MWLogicalServerNetworking() {}

	public static void setup() {
		ServerPlayNetworking.registerGlobalReceiver(REORDER_INVENTORY_C2S_PACKET, MWLogicalServerNetworking::onReorderInventoryPacket);
	}

	private static void onReorderInventoryPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		ReorderInventoryPacket packet = ReorderInventoryPacket.read(buf);

		if (packet == null) {
			log.warn("Failed to read reorder inventory packet from player {}!", player);
			return;
		}

		if (player.container == null) {
			log.warn("Player {} tried to reorder inventory without having an open container!", player);
			return;
		}

		if (packet.getSyncId() == player.playerContainer.syncId) {
			server.execute(() -> reorder(player, player.playerContainer, packet.getSlotMappings()));
		} else if (packet.getSyncId() == player.container.syncId) {
			server.execute(() -> reorder(player, player.container, packet.getSlotMappings()));
		}
	}

	private static void reorder(PlayerEntity player, Container screenHandler, int[] slotMapping) {
		if (!checkReorder(player, screenHandler, slotMapping)) {
			log.warn("Reorder inventory packet from player {} contains invalid data, ignoring!", player);
			return;
		}

		List<ItemStack> stacks = screenHandler.slots.stream().map(Slot::getStack).collect(Collectors.toList());

		for (int i = 0; i < slotMapping.length; i += 2) {
			int originSlotId = slotMapping[i];
			int destSlotId = slotMapping[i + 1];

			screenHandler.slots.get(destSlotId).setStack(stacks.get(originSlotId));
		}
	}

	private static boolean checkReorder(PlayerEntity player, Container screenHandler, int[] slotMappings) {
		if (slotMappings.length < 4) {
			log.warn("Reorder inventory packet contains too few slots!");
			return false;
		}

		IntSet requestedSlots = new IntAVLTreeSet();
		Inventory targetInv;

		Slot firstSlot = screenHandler.slots.get(slotMappings[0]);
		targetInv = firstSlot.inventory;

		for (int i = 0; i < slotMappings.length; i += 2) {
			int originSlotId = slotMappings[i];
			int destSlotId = slotMappings[i + 1];

			if (!checkReorderSlot(screenHandler, originSlotId, targetInv)) {
				return false;
			}
			if (!requestedSlots.add(originSlotId)) {
				log.warn("Reorder inventory packet contains duplicate origin slot {}!", originSlotId);
				return false;
			}

			if (!checkReorderSlot(screenHandler, destSlotId, targetInv)) {
				return false;
			}

			if (originSlotId == destSlotId) {
				continue;
			}

			Slot originSlot = screenHandler.getSlot(originSlotId);
			if (!originSlot.canTakeItems(player)) {
				log.warn("Player {} tried to reorder slot {}, but that slot doesn't allow taking items!", player, originSlotId);
				return false;
			}
			Slot destSlot = screenHandler.getSlot(destSlotId);
			if (!destSlot.canInsert(originSlot.getStack())) {
				log.warn("Player {} tried to reorder slot {}, but that slot doesn't allow inserting the origin stack!", player, destSlotId);
				return false;
			}
		}

		for (int i = 1; i < slotMappings.length; i += 2) {
			int destSlotId = slotMappings[i];
			if (!requestedSlots.remove(destSlotId)) {
				log.warn("Reorder inventory packet contains duplicate destination slot or slot without origin: {}!", i);
				return false;
			}
		}
		if (!requestedSlots.isEmpty()) {
			log.error("Invalid state during checking reorder packet, please report this to the {} bug tracker. Requested slots: {}", MouseWheelie.MOD_NAME, requestedSlots);
			return false;
		}
		return true;
	}

	private static boolean checkReorderSlot(Container screenHandler, int slotId, Inventory targetInv) {
		Slot slot = screenHandler.getSlot(slotId);
		if (slot == null) {
			log.warn("Reorder inventory packet contains invalid slot id!");
			return false;
		}

		if (targetInv != slot.inventory) {
			log.warn("Reorder inventory packet contains slots from different inventories, first: {}, now: {}!", targetInv, slot.inventory);
			return false;
		}
		return true;
	}
}
