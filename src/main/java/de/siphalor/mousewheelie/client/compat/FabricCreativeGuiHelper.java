/*
 * Copyright 2021 Siphalor
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

package de.siphalor.mousewheelie.client.compat;

import net.fabricmc.fabric.impl.item.group.CreativeGuiExtensions;
import net.fabricmc.fabric.impl.item.group.FabricCreativeGuiComponents;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

public final class FabricCreativeGuiHelper {
	private final CreativeGuiExtensions fabricExtensions;

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

	public static int getCommonItemGroupsSize() {
		return FabricCreativeGuiComponents.COMMON_GROUPS.size();
	}
}
