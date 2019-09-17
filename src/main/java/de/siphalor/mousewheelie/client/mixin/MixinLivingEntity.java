package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.ClientCore;
import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.util.inventory.SlotRefiller;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
	@Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.BEFORE))
	protected void onItemUseFinish(CallbackInfo callbackInfo) {
		if((Object) this instanceof PlayerEntity && Config.eatRefill.value) {
			PlayerInventory playerInventory = ((PlayerEntity)(Object) this).inventory;
            SlotRefiller.set(playerInventory, playerInventory.getMainHandStack().copy());
			ClientCore.awaitSlotUpdate = true;
		}
	}
}
