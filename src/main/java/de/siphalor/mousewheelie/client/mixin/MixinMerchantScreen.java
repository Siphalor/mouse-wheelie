package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.util.IMerchantScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantScreen.class)
public abstract class MixinMerchantScreen implements IMerchantScreen {

	@Shadow private int field_19161;

	@Shadow protected abstract void syncRecipeIndex();

	@Shadow private int field_19163;

	@Override
	public void mouseWheelie_setRecipeId(int id) {
		field_19161 = id;
	}

	@Override
	public void mouseWheelie_syncRecipeId() {
		syncRecipeIndex();
	}

	@Override
	public int getRecipeIdOffset() {
		return field_19163;
	}
}
