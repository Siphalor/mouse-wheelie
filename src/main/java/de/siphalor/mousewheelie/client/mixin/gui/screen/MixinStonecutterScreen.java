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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StonecutterScreen.class)
public abstract class MixinStonecutterScreen extends HandledScreen<StonecutterScreenHandler> implements IContainerScreen {

	public MixinStonecutterScreen(StonecutterScreenHandler container, PlayerInventory playerInventory, Text name) {
		super(container, playerInventory, name);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		Slot slot = mouseWheelie_getSlotAt(mouseX, mouseY);

		if (slot != null) {
			new ContainerScreenHelper<>((StonecutterScreen) (Object) this, (slot1, data, slotActionType) -> InteractionManager.push(new InteractionManager.CallbackEvent(() -> {
				onMouseClick(slot1, ((ISlot) slot1).mouseWheelie_getInvSlot(), data, slotActionType);
				return new InteractionManager.GuiConfirmWaiter(1);
			}))).scroll(slot, scrollAmount < 0);
			return ScrollAction.SUCCESS;
		}

		return ScrollAction.PASS;
	}
}
