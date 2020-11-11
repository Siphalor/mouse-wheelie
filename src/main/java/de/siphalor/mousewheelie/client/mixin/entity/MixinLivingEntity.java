package de.siphalor.mousewheelie.client.mixin.entity;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
	@Shadow
	public abstract Hand getActiveHand();

	@Shadow
	protected ItemStack activeItemStack;

	@Inject(method = "consumeItem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
	protected void onItemUseFinish(CallbackInfo callbackInfo) {
		//noinspection ConstantConditions
		if ((Object) this instanceof PlayerEntity && MWConfig.refill.eat && activeItemStack.isEmpty()) {
			PlayerInventory playerInventory = ((PlayerEntity) (Object) this).getInventory();
			activeItemStack.setCount(1);
			MWClient.scheduleRefill(getActiveHand(), playerInventory, activeItemStack.copy());
			activeItemStack.setCount(0);
		}
	}
}
