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

package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class ScrollKeyBinding extends AmecsKeyBinding implements PriorityKeyBinding {
	private final boolean scrollDown;

	public ScrollKeyBinding(Identifier id, String category, boolean scrollDown) {
		super(id, InputUtil.Type.MOUSE, scrollDown ? KeyBindingUtils.MOUSE_SCROLL_DOWN : KeyBindingUtils.MOUSE_SCROLL_UP, category, new KeyModifiers());
		this.scrollDown = scrollDown;
	}

	@Override
	public boolean onPressedPriority() {
		return MWClient.triggerScroll(MWClient.getMouseX(), MWClient.getMouseY(), scrollDown == MWConfig.scrolling.invert ? -1D : 1D);
	}
}
