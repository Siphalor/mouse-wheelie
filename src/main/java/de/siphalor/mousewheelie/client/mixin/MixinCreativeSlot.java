/*
 * Copyright 2020 Siphalor and contributors
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

package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.util.inject.ISlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.inventory.Slot;

@Mixin(targets = "net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$SlotWrapper")
public class MixinCreativeSlot implements ISlot {

	@Shadow
	@Final
	/*# if MC_VERSION_NUMBER >= 260100*/private/*# end */
	Slot target;

	@Override
	public int mouseWheelie_getIndexInInv() {
		return ((ISlot) target).mouseWheelie_getIndexInInv();
	}

	@Override
	public int mouseWheelie_getIdInContainer() {
		return ((ISlot) target).mouseWheelie_getIdInContainer();
	}
}
