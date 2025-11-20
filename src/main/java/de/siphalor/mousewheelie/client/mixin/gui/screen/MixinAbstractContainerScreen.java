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

package de.siphalor.mousewheelie.client.mixin.gui.screen;

import com.google.common.base.Suppliers;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.BundleDragMode;
import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.inventory.sort.InventorySorter;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IContainerScreen;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
//- import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
//- import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
//- import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
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
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen implements IContainerScreen {
	protected MixinAbstractContainerScreen(Component textComponent_1) {
		super(textComponent_1);
	}

	//# if MC_VERSION_NUMBER >= 12103
	@Shadow
	protected abstract Slot getHoveredSlot(double x, double y);

	@Unique
	private @Nullable Slot findSlot(double x, double y) {
		return getHoveredSlot(x, y);
	}
	//# else
	//- @Shadow
	//- protected abstract Slot findSlot(double x, double y);
	//# end

	@Shadow
	protected abstract void slotClicked(Slot slot_1, int int_1, int int_2, ClickType slotActionType_1);

	@Shadow
	@Final
	protected AbstractContainerMenu menu;

	@Shadow
	protected Slot hoveredSlot;

	@Shadow
	private @Nullable Slot clickedSlot;
	@Shadow
	protected boolean isQuickCrafting;

	//# if MC_VERSION_NUMBER >= 12103
	@Shadow
	@Final
	private List<ItemSlotMouseAction> itemSlotMouseActions;
	//# end

	@SuppressWarnings({"ConstantConditions", "unchecked"})
	@Unique
	private final Supplier<ContainerScreenHelper<AbstractContainerScreen<AbstractContainerMenu>>> screenHelper = Suppliers.memoize(
			() -> ContainerScreenHelper.of((AbstractContainerScreen<AbstractContainerMenu>) (Object) this, (slot, data, slotActionType) -> new InteractionManager.CallbackEvent(() -> {
				slotClicked(slot, ((ISlot) slot).mouseWheelie_getIdInContainer(), data, slotActionType);
				return InteractionManager.TICK_WAITER;
			}, true))
	);

	@Unique
	private Slot lastBundleInteractionSlot;
	@Unique
	private BundleDragMode bundleDragMode;

	@Inject(method = "mouseDragged", at = @At("RETURN"))
	//# if MC_VERSION_NUMBER >= 12109
	public void onMouseDragged(MouseButtonEvent event, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
		int button = event.button();
		double x = event.x();
		double y = event.y();
	//# else
	//- public void onMouseDragged(double x, double y, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
	//# end
		Collection<Slot> slots = Collections.emptyList();
		Slot hoveredSlot = findSlot(x, y);

		if (MouseWheelie.config.general.betterFastDragging) {
			double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			if (dist > 16.0) {
				slots = new ArrayList<>();
				if (hoveredSlot != null) {
					slots.add(hoveredSlot);
				}

				for (int i = 0; i < Mth.floor(dist / 16.0); i++) {
					double curX = x + deltaX - deltaX / dist * 16.0 * i;
					double curY = y + deltaY - deltaY / dist * 16.0 * i;
					Slot curSlot = findSlot(curX, curY);
					if (curSlot != null) {
						slots.add(curSlot);
					}
				}
			}
		}
		if (slots.isEmpty()) {
			if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty()) {
				slots = Collections.singletonList(hoveredSlot);
			} else {
				return;
			}
		}

		ContainerScreenHelper<?> screenHelper = this.screenHelper.get();
		if (button == 0) { // Left mouse button
			if (MouseWheelie.config.general.enableDropModifier && MWClient.DROP_MODIFIER.isDown()) {
				for (Slot slot : slots) {
					screenHelper.dropStackLocked(slot);
				}
			} else if (MWClient.WHOLE_STACK_MODIFIER.isDown()) {
				for (Slot slot : slots) {
					screenHelper.sendStackLocked(slot);
				}
			} else if (MWClient.ALL_OF_KIND_MODIFIER.isDown()) {
				for (Slot slot : slots) {
					screenHelper.sendAllOfAKind(slot);
				}
			}
		} else if (button == 1) { // Right mouse button
			ItemStack cursorStack = menu.getCarried();

			if (!cursorStack.isEmpty() && bundleDragMode != null && cursorStack.getItem() instanceof BundleItem item) {
				Slot lastSlot = null;
				for (Slot slot : slots) {
					if (slot == lastBundleInteractionSlot) {
						continue;
					}
					if (bundleDragMode == BundleDragMode.AUTO) {
						if (slot.getItem().isEmpty()) {
							if (item.isBarVisible(cursorStack)) {
								bundleDragMode = BundleDragMode.PUTTING_OUT;
							}
						} else {
							bundleDragMode = BundleDragMode.PICKING_UP;
						}
					}
					if (bundleDragMode == BundleDragMode.PICKING_UP && slot.getItem().isEmpty()) {
						continue;
					}
					if (bundleDragMode == BundleDragMode.PUTTING_OUT && !slot.getItem().isEmpty()) {
						continue;
					}

					slotClicked(slot, slot.index, 1, ClickType.PICKUP);

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
	//# if MC_VERSION_NUMBER >= 12109
	public void onMouseClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		int button = event.button();
		double x = event.x();
		double y = event.y();
	//# else
	//- public void onMouseClick(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
	//# end
		if (button == 0) {
			Slot hoveredSlot = findSlot(x, y);
			if (hoveredSlot == null) {
				return;
			}

			boolean success = true;
			if (MouseWheelie.config.general.enableDropModifier && MWClient.DROP_MODIFIER.isDown()) {
				if (MWClient.ALL_OF_KIND_MODIFIER.isDown()) {
					if (MWClient.WHOLE_STACK_MODIFIER.isDown()) {
						screenHelper.get().dropAllFrom(hoveredSlot);
					} else {
						screenHelper.get().dropAllOfAKind(hoveredSlot);
					}
				} else {
					slotClicked(hoveredSlot, ((ISlot) hoveredSlot).mouseWheelie_getIdInContainer(), 1, ClickType.THROW);
				}
			} else if (MWClient.ALL_OF_KIND_MODIFIER.isDown()) {
				if (MWClient.WHOLE_STACK_MODIFIER.isDown()) {
					screenHelper.get().sendAllFrom(hoveredSlot);
				} else {
					screenHelper.get().sendAllOfAKind(hoveredSlot);
				}
			} else if (MWClient.DEPOSIT_MODIFIER.isDown()) {
				screenHelper.get().depositAllFrom(hoveredSlot);
			} else if (MWClient.RESTOCK_MODIFIER.isDown()) {
				if (MWClient.WHOLE_STACK_MODIFIER.isDown()) {
					screenHelper.get().restockAll(hoveredSlot);
				} else {
					screenHelper.get().restockAllOfAKind(hoveredSlot);
				}
			} else {
				success = false;
			}
			if (success) {
				cir.setReturnValue(true);
			}
		} else if (button == 1) {
			ItemStack cursorStack = menu.getCarried();
			if (!cursorStack.isEmpty() && MouseWheelie.config.general.enableBundleDragging && cursorStack.getItem() instanceof BundleItem item) {
				Slot hoveredSlot = findSlot(x, y);
				if (hoveredSlot == null) {
					bundleDragMode = BundleDragMode.AUTO;
				} else if (hoveredSlot.getItem().isEmpty()) {
					if (item.isBarVisible(cursorStack)) {
						bundleDragMode = BundleDragMode.PUTTING_OUT;
					} else {
						bundleDragMode = BundleDragMode.AUTO;
					}
				} else {
					bundleDragMode = BundleDragMode.PICKING_UP;
				}
				if (hoveredSlot != null) {
					slotClicked(hoveredSlot, hoveredSlot.index, 1, ClickType.PICKUP);
				}
			}
		}
	}

	// Fires on mouse up
	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	//# if MC_VERSION_NUMBER >= 12109
	public void onMouseRelease(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
	//# else
	//- public void onMouseRelease(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
	//# end
		if (bundleDragMode != null) {
			clickedSlot = null;
			isQuickCrafting = false;
			cir.setReturnValue(true);
		}
		lastBundleInteractionSlot = null;
		bundleDragMode = null;
	}

	@Override
	public Slot mouseWheelie_getSlotAt(double mouseX, double mouseY) {
		return findSlot(mouseX, mouseY);
	}

	@Override
	public ScrollAction mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		if (MouseWheelie.config.scrolling.enable) {
			//# if MC_VERSION_NUMBER >= 12103
			Slot hoveredSlot = findSlot(mouseX, mouseY);

			if (MouseWheelie.config.scrolling.preferStackSpecialScrollActions) {
				if (!MWClient.isScrollModeToggled()) {
					for (ItemSlotMouseAction slotMouseAction : itemSlotMouseActions) {
						if (slotMouseAction.matches(hoveredSlot)) {
							return ScrollAction.ABORT;
						}
					}
				}
			} else if (MWClient.isScrollModeToggled()) {
				return ScrollAction.FAILURE;
			}
			//# else
			//- if (MWClient.isScrollModeToggled()) {
			//- 	return ScrollAction.FAILURE;
			//- }
			//- Slot hoveredSlot = findSlot(mouseX, mouseY);
			//# end

			if (hoveredSlot == null)
				return ScrollAction.PASS;
			if (hoveredSlot.getItem().isEmpty())
				return ScrollAction.PASS;

			//noinspection ConstantConditions
			if (scrollAmount < 0 && (Object) this instanceof InventoryScreen) {
				EquipmentSlot equipmentSlot = getEquipmentSlot(hoveredSlot.getItem());
				//# if MC_VERSION_NUMBER >= 12100
				if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				//# else
				//- if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
				//# end
					int hoveredSlotId = ((ISlot) hoveredSlot).mouseWheelie_getIdInContainer();
					InteractionManager.pushClickEvent(menu.containerId, hoveredSlotId, 0, ClickType.PICKUP);
					InteractionManager.pushClickEvent(menu.containerId, 8 - equipmentSlot.getIndex(), 0, ClickType.PICKUP);
					InteractionManager.pushClickEvent(menu.containerId, hoveredSlotId, 0, ClickType.PICKUP);
					return ScrollAction.SUCCESS;
				}
			}

			screenHelper.get().scroll(hoveredSlot, scrollAmount < 0);
			return ScrollAction.SUCCESS;
		}
		return ScrollAction.PASS;
	}

	@Unique
	private static EquipmentSlot getEquipmentSlot(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12103
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (equippable != null) {
			return equippable.slot();
		}
		return EquipmentSlot.MAINHAND;
		//# elif MC_VERSION_NUMBER >= 12100
		//- if (stack.getItem() instanceof Equipable) {
		//- 	return ((Equipable) stack.getItem()).getEquipmentSlot();
		//- }
		//- return EquipmentSlot.MAINHAND;
		//# else
		//- return Mob.getEquipmentSlotForItem(stack);
		//# end
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean mouseWheelie_triggerSort() {
		if (hoveredSlot == null)
			return false;
		Player player = Minecraft.getInstance().player;
		if (player.getAbilities().instabuild
				//# if MC_VERSION_NUMBER >= 12109
				&& GLFW.glfwGetMouseButton(minecraft.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) != 0
				//# else
				//- && GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) != 0
				//# end
				&& (!hoveredSlot.getItem().isEmpty() == menu.getCarried().isEmpty()))
			return false;
		InventorySorter sorter = new InventorySorter(screenHelper.get(), (AbstractContainerScreen<?>) (Object) this, hoveredSlot);
		SortMode sortMode;
		//# if MC_VERSION_NUMBER >= 12109
		if (Minecraft.getInstance().hasShiftDown()) {
			sortMode = MouseWheelie.config.sort.shiftSort;
		} else if (Minecraft.getInstance().hasControlDown()) {
		//# else
		//- if (hasShiftDown()) {
		//- 	sortMode = MouseWheelie.config.sort.shiftSort;
		//- } else if (hasControlDown()) {
		//# end
			sortMode = MouseWheelie.config.sort.controlSort;
		} else {
			sortMode = MouseWheelie.config.sort.primarySort;
		}
		if (sortMode == null) return false;
		sorter.sort(sortMode);
		return true;
	}
}
