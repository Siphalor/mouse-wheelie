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

package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class SortKeyBinding extends AmecsKeyBinding implements PriorityKeyBinding {
	public SortKeyBinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}

	@Override
	public boolean onPressedPriority() {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen instanceof IContainerScreen)
			return ((IContainerScreen) currentScreen).mouseWheelie_triggerSort();
		return false;
	}
}
