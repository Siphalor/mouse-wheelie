package de.siphalor.mousewheelie;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public class MouseWheelie implements ModInitializer {
	public static final String MOD_ID = "mousewheelie";

	@Override
	public void onInitialize() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (world instanceof ServerWorld) {
				ItemStack stack = player.getStackInHand(hand);
				EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
				if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
					ItemStack equipmentStack = player.getEquippedStack(equipmentSlot);
					if (!equipmentStack.isEmpty()) {
						player.setStackInHand(hand, equipmentStack);
						player.setEquippedStack(equipmentSlot, stack);
						return ActionResult.SUCCESS;
					}
				}
			}
			return ActionResult.PASS;
		});
	}
}
