package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
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

	@Inject(method = "setCount", at = @At("HEAD"))
	public void onSetCount(int newCount, CallbackInfo callbackInfo) {
		if (newCount == 0) {
			ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
			//noinspection ConstantConditions
			if (playerEntity.getMainHandStack() == (Object) this) {
				SlotRefiller.set(playerEntity.inventory, copy());
				MWClient.awaitSlotUpdate = true;
			}
		}
	}
}
