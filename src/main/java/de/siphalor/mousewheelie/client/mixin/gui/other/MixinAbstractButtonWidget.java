package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.client.util.accessors.ISpecialClickableButtonWidget;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractButtonWidget.class)
public abstract class MixinAbstractButtonWidget {
	@Shadow
	protected abstract boolean clicked(double double_1, double double_2);

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/AbstractButtonWidget;isValidClickButton(I)Z"), cancellable = true)
	public void mouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (this.clicked(x, y)) {
			if (this instanceof ISpecialClickableButtonWidget) {
				if (((ISpecialClickableButtonWidget) this).mouseClicked(button))
					callbackInfoReturnable.setReturnValue(true);
			}
		}
	}
}
