package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.MWClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class MixinItemStack {

	@Shadow
	public abstract ItemStack copy();

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "setCount", at = @At("HEAD"))
	public void onSetCount(int newCount, CallbackInfo callbackInfo) {
		if (newCount == 0) {
			ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
			if (playerEntity.getMainHandStack() == (Object) this) {
				MWClient.scheduleRefill(Hand.MAIN_HAND, playerEntity.inventory, copy());
			} else if (playerEntity.getOffHandStack() == (Object) this) {
				MWClient.scheduleRefill(Hand.OFF_HAND, playerEntity.inventory, copy());
			}
		}
	}
}
