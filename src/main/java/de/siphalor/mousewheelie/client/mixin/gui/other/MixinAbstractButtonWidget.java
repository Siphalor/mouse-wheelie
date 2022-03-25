/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client.mixin.gui.other;

import de.siphalor.mousewheelie.client.util.accessors.ISpecialClickableButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClickableWidget.class)
public abstract class MixinAbstractButtonWidget {
	@Shadow
	protected abstract boolean clicked(double double_1, double double_2);

	@Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;isValidClickButton(I)Z"), cancellable = true)
	public void mouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (this.clicked(x, y)) {
			if (this instanceof ISpecialClickableButtonWidget) {
				if (((ISpecialClickableButtonWidget) this).mouseClicked(button))
					callbackInfoReturnable.setReturnValue(true);
			}
		}
	}
}
