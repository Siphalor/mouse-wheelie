package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.util.accessors.IMerchantScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantScreen.class)
public abstract class MixinMerchantScreen implements IMerchantScreen {

	@Shadow private int selectedIndex;

	@Shadow protected abstract void syncRecipeIndex();

	@Shadow private int indexStartOffset;

	@Override
	public void mouseWheelie_setRecipeId(int id) {
		selectedIndex = id;
	}

	@Override
	public void mouseWheelie_syncRecipeId() {
		syncRecipeIndex();
	}

	@Override
	public int getRecipeIdOffset() {
		return indexStartOffset;
	}
}
