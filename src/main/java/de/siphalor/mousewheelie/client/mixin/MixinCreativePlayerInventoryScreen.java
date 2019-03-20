package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.Core;
import de.siphalor.mousewheelie.util.FabricCreativeGuiHelper;
import de.siphalor.mousewheelie.util.ISpecialScrollableScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryScreen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.TextComponent;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativePlayerInventoryScreen.class)
public abstract class MixinCreativePlayerInventoryScreen extends AbstractPlayerInventoryScreen<CreativePlayerInventoryScreen.CreativeContainer> implements ISpecialScrollableScreen {

	@Shadow private static int selectedTab;

	@Shadow protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	public MixinCreativePlayerInventoryScreen(CreativePlayerInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public boolean mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		// Exact box matching:
		//if(mouseX >= this.left && mouseX < this.left + this.width && ((mouseY >= this.top - 28 && mouseY < this.top + 4) || (mouseY >= this.top + this.height - 4 && mouseY < this.top + this.height + 28))) {
		// Rough matching:
		if(mouseY < this.top + 4 || mouseY >= this.top + this.height - 4) {
			if(FabricLoader.getInstance().isModLoaded("fabric")) {
				FabricCreativeGuiHelper helper = new FabricCreativeGuiHelper((CreativePlayerInventoryScreen)(Object) this);
				int newIndex = MathHelper.clamp(this.selectedTab + (int) Math.round(scrollAmount * Core.scrollFactor), 0, ItemGroup.GROUPS.length - 1);
				int newPage = helper.getPageForTabIndex(newIndex);
				if(newPage < helper.getCurrentPage())
					helper.previousPage();
				if(newPage > helper.getCurrentPage())
					helper.nextPage();
				setSelectedTab(ItemGroup.GROUPS[newIndex]);
			} else
				setSelectedTab(ItemGroup.GROUPS[MathHelper.clamp((int) (this.selectedTab + Math.round(scrollAmount * Core.scrollFactor)), 0, ItemGroup.GROUPS.length - 1)]);
            return false;
		}
		return false;
	}
}
