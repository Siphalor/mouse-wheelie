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

package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.client.inventory.ToolPicker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class PickToolKeyBinding extends AmecsKeyBinding implements PriorityKeyBinding {
	public PickToolKeyBinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}

	@Override
	public boolean onPressedPriority() {
		if (MinecraftClient.getInstance().currentScreen != null) return false;
		PlayerEntity playerEntity = MinecraftClient.getInstance().player;
		if (playerEntity != null) {
			HitResult hitResult = playerEntity.raycast(4.5D, 0.0F, false);
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				return new ToolPicker(playerEntity.getInventory()).pickToolFor(playerEntity.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()));
			} else {
				return new ToolPicker(playerEntity.getInventory()).pickWeapon();
			}
		}
		return false;
	}
}
