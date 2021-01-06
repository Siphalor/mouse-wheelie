/*
 * Copyright 2020 Siphalor
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

package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;

@Environment(EnvType.CLIENT)
public class ToolPicker {
	PlayerInventory inventory;

	static int lastToolPickSlot = -1;

	public ToolPicker(PlayerInventory inventory) {
		this.inventory = inventory;
	}

	public int findToolFor(BlockState blockState) {
		float bestBreakSpeed = 1.0F;
		int bestSpeedSlot = -1;
		int invSize = (MWConfig.toolPicking.pickFromInventory ? inventory.main.size() : 9);
		for (int i = 1; i <= invSize; i++) {
			int index = (i + lastToolPickSlot) % invSize;
			if (index == inventory.selectedSlot) continue;
			ItemStack stack = inventory.main.get(index);
			if (stack.isEffectiveOn(blockState)) {
				return index;
			} else {
				float breakSpeed = stack.getMiningSpeedMultiplier(blockState);
				if (breakSpeed > bestBreakSpeed) {
					bestSpeedSlot = index;
					bestBreakSpeed = breakSpeed;
				}
			}
		}
		if (bestBreakSpeed == -1) {
			ItemStack stack = inventory.main.get(inventory.selectedSlot);
			if (stack.isEffectiveOn(blockState) || stack.getMiningSpeedMultiplier(blockState) > 1.0F)
				return inventory.selectedSlot;
		}
		return bestSpeedSlot;
	}

	public boolean pickToolFor(BlockState blockState) {
		return pick(findToolFor(blockState));
	}

	public int findWeapon() {
		int invSize = (MWConfig.toolPicking.pickFromInventory ? inventory.main.size() : 9);
		for (int i = 1; i <= invSize; i++) {
			int index = (i + lastToolPickSlot) % invSize;
			if (index == inventory.selectedSlot) continue;
			if (MWClient.isWeapon(inventory.main.get(index).getItem()))
				return index;
		}
		return -1;
	}

	public boolean pickWeapon() {
		return pick(findWeapon());
	}

	private boolean pick(int index) {
		lastToolPickSlot = index;
		if (index != -1 && index != inventory.selectedSlot) {
			PickFromInventoryC2SPacket packet = new PickFromInventoryC2SPacket(index);
			ClientSidePacketRegistry.INSTANCE.sendToServer(packet);
			return true;
		}
		return false;
	}
}
