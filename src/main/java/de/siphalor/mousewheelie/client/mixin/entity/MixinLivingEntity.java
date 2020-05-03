package de.siphalor.mousewheelie.client.mixin.entity;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.ClientCore;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public class MixinLivingEntity {
	@Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE))
	protected void onItemUseFinish(CallbackInfo callbackInfo) {
		//noinspection ConstantConditions
		if ((Object) this instanceof PlayerEntity && MouseWheelie.CONFIG.refill.eat) {
			PlayerInventory playerInventory = ((PlayerEntity) (Object) this).inventory;
			SlotRefiller.set(playerInventory, playerInventory.getMainHandStack().copy());
			ClientCore.awaitSlotUpdate = true;
		}
	}
}
