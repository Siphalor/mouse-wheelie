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

package de.siphalor.mousewheelie;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public class MouseWheelie implements ModInitializer {
	public static final String MOD_ID = "mousewheelie";

	@Override
	public void onInitialize() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!world.isClient()) {
				EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
				if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
					ItemStack equipmentStack = player.getEquippedStack(equipmentSlot);
					if (!equipmentStack.isEmpty()) {
						player.setStackInHand(hand, equipmentStack);
						player.equipStack(equipmentSlot, stack);
						return ActionResult.SUCCESS;
					}
				}
			}
			return ActionResult.PASS;
		});
	}
}
