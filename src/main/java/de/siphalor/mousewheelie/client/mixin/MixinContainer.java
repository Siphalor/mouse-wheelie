package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ScreenHandler.class)
public abstract class MixinContainer {
	@Shadow
	public abstract Slot getSlot(int int_1);

	private static boolean mouseWheelie_scheduleRefill = false;

	@Inject(method = "updateSlotStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;getSlot(I)Lnet/minecraft/screen/slot/Slot;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onSlotUpdate(List<ItemStack> itemStacks, CallbackInfo callbackInfo, int index) {
		//noinspection ConstantConditions
		if ((Object) this instanceof PlayerScreenHandler && MouseWheelie.CONFIG.refill.other) {
			PlayerInventory inventory = MinecraftClient.getInstance().player.inventory;
			if (inventory.selectedSlot == ((ISlot) getSlot(index)).mouseWheelie_getInvSlot()) {
				ItemStack stack = inventory.getMainHandStack();
				if (!stack.isEmpty() && itemStacks.get(index).isEmpty()) {
					mouseWheelie_scheduleRefill = true;
					SlotRefiller.set(inventory, stack.copy());
				}
			}
		}
	}

	@Inject(method = "updateSlotStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onSlotUpdated(List<ItemStack> stacks, CallbackInfo callbackInfo, int index) {
		if (mouseWheelie_scheduleRefill) {
			mouseWheelie_scheduleRefill = false;
			SlotRefiller.refill();
		}
	}

}
