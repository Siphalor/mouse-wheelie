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

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.inventory.sort.InventorySorter;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Lazy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("WeakerAccess")
@Mixin(HandledScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen implements IContainerScreen {
	protected MixinAbstractContainerScreen(Text textComponent_1) {
		super(textComponent_1);
	}

	@Shadow
	protected abstract Slot getSlotAt(double double_1, double double_2);

	@Shadow
	protected abstract void onMouseClick(Slot slot_1, int int_1, int int_2, SlotActionType slotActionType_1);

	@Shadow
	@Final
	protected ScreenHandler handler;

	@Shadow
	@Final
	protected PlayerInventory playerInventory;

	@Shadow
	protected Slot focusedSlot;

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	@Unique
	private final Lazy<ContainerScreenHelper<HandledScreen<ScreenHandler>>> screenHelper = new Lazy<>(
			() -> ContainerScreenHelper.of((HandledScreen<ScreenHandler>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType))
	);

	@Inject(method = "mouseDragged", at = @At("RETURN"))
	public void onMouseDragged(double x2, double y2, int button, double x1, double y1, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (button == 0) {
			Slot hoveredSlot = getSlotAt(x2, y2);
			if (hoveredSlot != null) {
				if (hasAltDown()) {
					onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.THROW);
				} else if (hasShiftDown()) {
					screenHelper.get().sendStack(hoveredSlot);
				} else if (hasControlDown()) {
					screenHelper.get().sendAllOfAKind(hoveredSlot);
				}
			}
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void onMouseClick(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (button == 0) {
			if (hasAltDown()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					if (hasControlDown()) {
						if (hasShiftDown()) {
							screenHelper.get().dropAllFrom(hoveredSlot);
						} else {
							screenHelper.get().dropAllOfAKind(hoveredSlot);
						}
					} else {
						onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.THROW);
						callbackInfoReturnable.setReturnValue(true);
					}
				}
			} else if (hasControlDown()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					if (hasShiftDown()) {
						screenHelper.get().sendAllFrom(hoveredSlot);
					} else {
						screenHelper.get().sendAllOfAKind(hoveredSlot);
					}
					callbackInfoReturnable.setReturnValue(true);
				}
			}
		}
	}

	@Override
	public Slot mouseWheelie_getSlotAt(double mouseX, double mouseY) {
		return getSlotAt(mouseX, mouseY);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		if (MWConfig.scrolling.enable) {
			if (hasAltDown()) return ScrollAction.FAILURE;
			Slot hoveredSlot = getSlotAt(mouseX, mouseY);
			if (hoveredSlot == null)
				return ScrollAction.PASS;
			if (hoveredSlot.getStack().isEmpty())
				return ScrollAction.PASS;

			//noinspection ConstantConditions
			if (scrollAmount < 0 && (Object) this instanceof InventoryScreen) {
				EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(hoveredSlot.getStack());
				if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
					InteractionManager.pushClickEvent(handler.syncId, hoveredSlot.id, 0, SlotActionType.PICKUP);
					InteractionManager.pushClickEvent(handler.syncId, 8 - equipmentSlot.getEntitySlotId(), 0, SlotActionType.PICKUP);
					InteractionManager.pushClickEvent(handler.syncId, hoveredSlot.id, 0, SlotActionType.PICKUP);
					return ScrollAction.SUCCESS;
				}
			}

			screenHelper.get().scroll(hoveredSlot, scrollAmount < 0);
			return ScrollAction.SUCCESS;
		}
		return ScrollAction.PASS;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean mouseWheelie_triggerSort() {
		if (focusedSlot == null)
			return false;
		if (playerInventory.player.abilities.creativeMode && MWClient.SORT_KEY_BINDING.isDefault() && (!focusedSlot.getStack().isEmpty() == playerInventory.getCursorStack().isEmpty()))
			return false;
		InventorySorter sorter = new InventorySorter((HandledScreen<?>) (Object) this, focusedSlot);
		SortMode sortMode;
		if (hasShiftDown()) {
			sortMode = MWConfig.sort.shiftSort;
		} else if (hasControlDown()) {
			sortMode = MWConfig.sort.controlSort;
		} else {
			sortMode = MWConfig.sort.primarySort;
		}
		if (sortMode == null) return false;
		sorter.sort(sortMode);
		return true;
	}
}
