package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.compat.FabricCreativeGuiHelper;
import de.siphalor.mousewheelie.client.inventory.CreativeContainerScreenHelper;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import de.siphalor.mousewheelie.client.util.accessors.ISpecialScrollableScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeContainer> implements ISpecialScrollableScreen, IContainerScreen {

	@Shadow
	private static int selectedTab;

	@Shadow
	protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	@Shadow
	private List<Slot> slots;

	@Shadow
	protected abstract void onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType);

	public MixinCreativeInventoryScreen(CreativeInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public boolean mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		// Exact box matching:
		//if(mouseX >= this.left && mouseX < this.left + this.width && ((mouseY >= this.top - 28 && mouseY < this.top + 4) || (mouseY >= this.top + this.height - 4 && mouseY < this.top + this.height + 28))) {
		// Rough matching:
		if (mouseY < this.y + 4 || mouseY >= this.y + this.containerHeight - 4) {
			if (FabricLoader.getInstance().isModLoaded("fabric") || FabricLoader.getInstance().isModLoaded("fabric-item-groups")) {
				FabricCreativeGuiHelper helper = new FabricCreativeGuiHelper((CreativeInventoryScreen)(Object) this);
				int newIndex = MathHelper.clamp(selectedTab + (int) Math.round(scrollAmount * Config.scrollFactor.value), 0, ItemGroup.GROUPS.length - 1);
				int newPage = helper.getPageForTabIndex(newIndex);
				if (newPage < helper.getCurrentPage())
					helper.previousPage();
				if (newPage > helper.getCurrentPage())
					helper.nextPage();
				setSelectedTab(ItemGroup.GROUPS[newIndex]);
			} else
				setSelectedTab(ItemGroup.GROUPS[MathHelper.clamp((int) (selectedTab + Math.round(scrollAmount * Config.scrollFactor.value)), 0, ItemGroup.GROUPS.length - 1)]);
			return true;
		}

		if (Config.enableItemScrolling.value && selectedTab != ItemGroup.INVENTORY.getIndex()) {
			Slot hoverSlot = this.mouseWheelie_getSlotAt(mouseX, mouseY);
			if (hoverSlot != null) {
				new CreativeContainerScreenHelper<>((CreativeInventoryScreen) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, ((ISlot) slot).mouseWheelie_getInvSlot(), data, slotActionType)).scroll(hoverSlot, scrollAmount < 0);
				return true;
			}
		}

		return false;
	}
}
