package de.siphalor.mousewheelie.client.compat;

import net.fabricmc.fabric.impl.item.group.CreativeGuiExtensions;
import net.fabricmc.fabric.impl.item.group.FabricCreativeGuiComponents;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

public final class FabricCreativeGuiHelper {
	CreativeGuiExtensions fabricExtensions;

	public FabricCreativeGuiHelper(CreativeInventoryScreen screen) {
		fabricExtensions = (CreativeGuiExtensions) screen;
	}

	public void nextPage() {
		fabricExtensions.fabric_nextPage();
	}

	public void previousPage() {
		fabricExtensions.fabric_previousPage();
	}

	public int getCurrentPage() {
		return fabricExtensions.fabric_currentPage();
	}

	public int getPageForTabIndex(int index) {
		return index < 12 ? 0 : (index - 12) / (12 - FabricCreativeGuiHelper.getCommonItemGroupsSize()) + 1;
	}

	public static final int getCommonItemGroupsSize() {
		return FabricCreativeGuiComponents.COMMON_GROUPS.size();
	}
}
