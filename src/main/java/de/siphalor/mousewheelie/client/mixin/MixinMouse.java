package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.util.IContainerScreen;
import de.siphalor.mousewheelie.client.util.IScrollableRecipeBook;
import de.siphalor.mousewheelie.client.util.ISpecialScrollableScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {

	@Shadow @Final private MinecraftClient client;

	@Shadow private double x;

	@Shadow private double y;

	private static double mouseWheelie_scrollX;

	@Inject(method = "onMouseScroll", at = @At("HEAD"))
	private void onMouseScrolledHead(long windowHandle, double scrollX, double scrollY, CallbackInfo callbackInfo) {
		mouseWheelie_scrollX = scrollX;
	}

	@ModifyVariable(method = "onMouseScroll", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
	private double changeMouseScroll(double old) {
		if(MinecraftClient.IS_SYSTEM_MAC && mouseWheelie_scrollX != 0) {
			return (this.client.options.discreteMouseScroll ? Math.signum(mouseWheelie_scrollX) : mouseWheelie_scrollX) * this.client.options.mouseWheelSensitivity;
		}
		return old;
	}

	@Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screen/Screen.mouseScrolled(DDD)Z", ordinal = 0), cancellable = true)
	private void onMouseScrolled(long windowHandle, double scrollX, double scrollY, CallbackInfo callbackInfo) {
        double mouseX = this.x * (double) this.client.method_22683().getScaledWidth() / (double) this.client.method_22683().getWidth();
		double mouseY = this.y * (double) this.client.method_22683().getScaledHeight() / (double) this.client.method_22683().getHeight();
		double scrollAmount = scrollY * this.client.options.mouseWheelSensitivity;
		if(this.client.currentScreen instanceof ISpecialScrollableScreen) {
			if(((ISpecialScrollableScreen) this.client.currentScreen).mouseWheelie_onMouseScrolledSpecial(mouseX, mouseY, scrollAmount)) {
				callbackInfo.cancel();
				return;
			}
		}
        if(this.client.currentScreen instanceof IContainerScreen) {
	        if(((IContainerScreen) this.client.currentScreen).mouseWheelie_onMouseScroll(mouseX, mouseY, scrollAmount)) {
		        callbackInfo.cancel();
		        return;
	        }
        }
        if(this.client.currentScreen instanceof IScrollableRecipeBook) {
	        if(((IScrollableRecipeBook) this.client.currentScreen).mouseWheelie_onMouseScrollRecipeBook(mouseX, mouseY, scrollAmount)) {
		        callbackInfo.cancel();
		        return;
	        }
        }
	}
}
