package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.Core;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.TextComponent;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativePlayerInventoryScreen.class)
public abstract class MixinCreativePlayerInventoryScreen extends AbstractPlayerInventoryScreen<CreativePlayerInventoryScreen.CreativeContainer> {

	@Shadow private static int selectedTab;

	@Shadow protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	public MixinCreativePlayerInventoryScreen(CreativePlayerInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Inject(method = "mouseScrolled", at = @At(value = "HEAD"), cancellable = true)
	public void mouseScrolled(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		// Exact box matching:
		//if(mouseX >= this.left && mouseX < this.left + this.width && ((mouseY >= this.top - 28 && mouseY < this.top + 4) || (mouseY >= this.top + this.height - 4 && mouseY < this.top + this.height + 28))) {
		// Rough matching:
		if(mouseY < this.top + 4 || mouseY >= this.top + this.height - 4) {
			setSelectedTab(ItemGroup.GROUPS[MathHelper.clamp((int) (this.selectedTab + Math.round(amount * Core.scrollFactor)), 0, ItemGroup.GROUPS.length - 1)]);
            callbackInfoReturnable.setReturnValue(true);
		}
	}
}
