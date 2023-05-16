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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

import com.google.common.base.Suppliers;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.inventory.BundleDragMode;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.inventory.sort.InventorySorter;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

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
	protected Slot focusedSlot;

	@Shadow
	private @Nullable Slot touchDragSlotStart;
	@Shadow
	protected boolean cursorDragging;
	@SuppressWarnings({"ConstantConditions", "unchecked"})
	@Unique
	private final Supplier<ContainerScreenHelper<HandledScreen<ScreenHandler>>> screenHelper = Suppliers.memoize(
			() -> ContainerScreenHelper.of((HandledScreen<ScreenHandler>) (Object) this, (slot, data, slotActionType) -> new InteractionManager.CallbackEvent(() -> {
				onMouseClick(slot, slot.id, data, slotActionType);
				return InteractionManager.TICK_WAITER;
			}, true))
	);

	@Unique
	private Slot lastBundleInteractionSlot;
	@Unique
	private BundleDragMode bundleDragMode;

	@Inject(method = "mouseDragged", at = @At("RETURN"))
	public void onMouseDragged(double x, double y, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		Collection<Slot> slots = Collections.emptyList();
		Slot hoveredSlot = getSlotAt(x, y);

		if (MWConfig.general.betterFastDragging) {
			double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			if (dist > 16.0) {
				slots = new ArrayList<>();
				if (hoveredSlot != null) {
					slots.add(hoveredSlot);
				}

				for (int i = 0; i < MathHelper.floor(dist / 16.0); i++) {
					double curX = x + deltaX - deltaX / dist * 16.0 * i;
					double curY = y + deltaY - deltaY / dist * 16.0 * i;
					Slot curSlot = getSlotAt(curX, curY);
					if (curSlot != null) {
						slots.add(curSlot);
					}
				}
			}
		}
		if (slots.isEmpty()) {
			if (hoveredSlot != null) {
				slots = Collections.singletonList(hoveredSlot);
			} else {
				return;
			}
		}

		ContainerScreenHelper<?> screenHelper = this.screenHelper.get();
		if (button == 0) { // Left mouse button
			if (MWConfig.general.enableDropModifier && MWClient.DROP_MODIFIER.isPressed()) {
				for (Slot slot : slots) {
					screenHelper.dropStackLocked(slot);
				}
			} else if (MWClient.WHOLE_STACK_MODIFIER.isPressed()) {
				for (Slot slot : slots) {
					screenHelper.sendStackLocked(slot);
				}
			} else if (MWClient.ALL_OF_KIND_MODIFIER.isPressed()) {
				for (Slot slot : slots) {
					screenHelper.sendAllOfAKind(slot);
				}
			}
		} else if (button == 1) { // Right mouse button
			ItemStack cursorStack = handler.getCursorStack();

			if (!cursorStack.isEmpty() && bundleDragMode != null && cursorStack.getItem() instanceof BundleItem item) {
				Slot lastSlot = null;
				for (Slot slot : slots) {
					if (slot == lastBundleInteractionSlot) {
						continue;
					}
					if (bundleDragMode == BundleDragMode.AUTO) {
						if (slot.getStack().isEmpty()) {
							if (item.isItemBarVisible(cursorStack)) {
								bundleDragMode = BundleDragMode.PUTTING_OUT;
							}
						} else {
							bundleDragMode = BundleDragMode.PICKING_UP;
						}
					}
					if (bundleDragMode == BundleDragMode.PICKING_UP && slot.getStack().isEmpty()) {
						continue;
					}
					if (bundleDragMode == BundleDragMode.PUTTING_OUT && !slot.getStack().isEmpty()) {
						continue;
					}

					onMouseClick(slot, slot.id, 1, SlotActionType.PICKUP);

					lastSlot = slot;
				}
				if (lastSlot != null) {
					lastBundleInteractionSlot = lastSlot;
				}
			}
		}
	}

	// Fires on mouse down
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void onMouseClick(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (button == 0) {
			if (MWConfig.general.enableDropModifier && MWClient.DROP_MODIFIER.isPressed()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					if (MWClient.ALL_OF_KIND_MODIFIER.isPressed()) {
						if (MWClient.WHOLE_STACK_MODIFIER.isPressed()) {
							screenHelper.get().dropAllFrom(hoveredSlot);
						} else {
							screenHelper.get().dropAllOfAKind(hoveredSlot);
						}
					} else {
						onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.THROW);
					}
					callbackInfoReturnable.setReturnValue(true);
				}
			} else if (MWClient.ALL_OF_KIND_MODIFIER.isPressed()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					if (MWClient.WHOLE_STACK_MODIFIER.isPressed()) {
						screenHelper.get().sendAllFrom(hoveredSlot);
					} else {
						screenHelper.get().sendAllOfAKind(hoveredSlot);
					}
					callbackInfoReturnable.setReturnValue(true);
				}
			} else if (MWClient.DEPOSIT_MODIFIER.isPressed()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					screenHelper.get().depositAllFrom(hoveredSlot);
					callbackInfoReturnable.setReturnValue(true);
				}
			} else if (MWClient.RESTOCK_MODIFIER.isPressed()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					if (MWClient.WHOLE_STACK_MODIFIER.isPressed()) {
						screenHelper.get().restockAll(hoveredSlot);
					} else {
						screenHelper.get().restockAllOfAKind(hoveredSlot);
					}
					callbackInfoReturnable.setReturnValue(true);
				}
			}
		} else if (button == 1) {
			ItemStack cursorStack = handler.getCursorStack();
			if (!cursorStack.isEmpty() && MWConfig.general.enableBundleDragging && cursorStack.getItem() instanceof BundleItem item) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot == null) {
					bundleDragMode = BundleDragMode.AUTO;
				} else if (hoveredSlot.getStack().isEmpty()) {
					if (item.isItemBarVisible(cursorStack)) {
						bundleDragMode = BundleDragMode.PUTTING_OUT;
					} else {
						bundleDragMode = BundleDragMode.AUTO;
					}
				} else {
					bundleDragMode = BundleDragMode.PICKING_UP;
				}
				if (hoveredSlot != null) {
					onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.PICKUP);
				}
			}
		}
	}

	// Fires on mouse up
	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	public void onMouseRelease(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
		if (bundleDragMode != null) {
			touchDragSlotStart = null;
			cursorDragging = false;
			cir.setReturnValue(true);
		}
		lastBundleInteractionSlot = null;
		bundleDragMode = null;
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
		PlayerEntity player = MinecraftClient.getInstance().player;
		if (player.getAbilities().creativeMode
				&& GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) != 0
				&& (!focusedSlot.getStack().isEmpty() == handler.getCursorStack().isEmpty()))
			return false;
		InventorySorter sorter = new InventorySorter(screenHelper.get(), (HandledScreen<?>) (Object) this, focusedSlot);
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
