package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.util.FabricCreativeGuiHelper;
import de.siphalor.mousewheelie.client.util.IContainerScreen;
import de.siphalor.mousewheelie.client.util.ISpecialScrollableScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeContainer> implements ISpecialScrollableScreen, IContainerScreen {

	@Shadow private static int selectedTab;

	@Shadow protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	public MixinCreativeInventoryScreen(CreativeInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public boolean mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		return false;
	}

	@Override
	public boolean mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		// Exact box matching:
		//if(mouseX >= this.left && mouseX < this.left + this.width && ((mouseY >= this.top - 28 && mouseY < this.top + 4) || (mouseY >= this.top + this.height - 4 && mouseY < this.top + this.height + 28))) {
		// Rough matching:
		if(mouseY < this.top + 4 || mouseY >= this.top + this.containerHeight - 4) {
			if(FabricLoader.getInstance().isModLoaded("fabric") || FabricLoader.getInstance().isModLoaded("fabric-item-groups")) {
				FabricCreativeGuiHelper helper = new FabricCreativeGuiHelper((CreativeInventoryScreen)(Object) this);
				int newIndex = MathHelper.clamp(selectedTab + (int) Math.round(scrollAmount * Config.scrollFactor.value), 0, ItemGroup.GROUPS.length - 1);
				int newPage = helper.getPageForTabIndex(newIndex);
				if(newPage < helper.getCurrentPage())
					helper.previousPage();
				if(newPage > helper.getCurrentPage())
					helper.nextPage();
				setSelectedTab(ItemGroup.GROUPS[newIndex]);
			} else
				setSelectedTab(ItemGroup.GROUPS[MathHelper.clamp((int) (selectedTab + Math.round(scrollAmount * Config.scrollFactor.value)), 0, ItemGroup.GROUPS.length - 1)]);
            return false;
		}
		return false;
	}
}
