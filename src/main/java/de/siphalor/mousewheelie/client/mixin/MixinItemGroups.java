package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.MWClient;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroups.class)
public class MixinItemGroups {
	@Inject(method = "updateDisplayParameters", at = @At("TAIL"))
	private static void onUpdateDisplayParameters(CallbackInfoReturnable<Boolean> cir) {
		MWClient.refreshItemSearchPositionLookup();
	}
}
