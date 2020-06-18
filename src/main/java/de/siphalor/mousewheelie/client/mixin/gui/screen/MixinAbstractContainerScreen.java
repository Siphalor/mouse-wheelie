package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.MouseWheelie;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

	@Inject(method = "mouseDragged", at = @At("RETURN"))
	public void onMouseDragged(double x2, double y2, int button, double x1, double y1, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (button == 0) {
			Slot hoveredSlot = getSlotAt(x2, y2);
			if (hoveredSlot != null) {
				boolean alt = hasAltDown();
				boolean ctrl = hasControlDown();
				boolean shift = hasShiftDown();
				if (!ctrl) {
					if (alt) {
						onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.THROW);
					} else if (shift) {
						onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.QUICK_MOVE);
					}
				} else {
					@SuppressWarnings("ConstantConditions")
					ContainerScreenHelper<?> containerScreenHelper = new ContainerScreenHelper<>((HandledScreen<?>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType));
					if (alt) {
						if (shift) {
							containerScreenHelper.dropAllFrom(hoveredSlot);
						} else {
							containerScreenHelper.dropAllOfAKind(hoveredSlot);
						}
					} else {
						if (shift) {
							containerScreenHelper.sendAllFrom(hoveredSlot);
						} else {
							containerScreenHelper.sendAllOfAKind(hoveredSlot);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void onMouseClick(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (button == 0) {
			Slot hoveredSlot = getSlotAt(x, y);
			if (hoveredSlot != null) {
				boolean alt = hasAltDown();
				boolean ctrl = hasControlDown();
				boolean shift = hasShiftDown();
				if (alt && !ctrl) {
					onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.THROW);
					callbackInfoReturnable.setReturnValue(true);
				} else if (ctrl) {
					ContainerScreenHelper<?> containerScreenHelper = new ContainerScreenHelper<>((HandledScreen<?>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType));
					if (alt) {
						if (shift) {
							containerScreenHelper.dropAllFrom(hoveredSlot);
						} else {
							containerScreenHelper.dropAllOfAKind(hoveredSlot);
						}
					} else {
						if (shift) {
							containerScreenHelper.sendAllFrom(hoveredSlot);
						} else {
							containerScreenHelper.sendAllOfAKind(hoveredSlot);
						}
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
		if (MouseWheelie.CONFIG.scrolling.enable) {
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

			//noinspection ConstantConditions
			ContainerScreenHelper<?> containerScreenHelper = new ContainerScreenHelper<>((HandledScreen<?>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType));
			containerScreenHelper.scroll(hoveredSlot, scrollAmount < 0);

			return ScrollAction.SUCCESS;
		}
		return ScrollAction.PASS;
	}

	@Override
	public boolean mouseWheelie_triggerSort() {
		if (focusedSlot == null)
			return false;
		if (playerInventory.player.abilities.creativeMode && (!focusedSlot.getStack().isEmpty() == playerInventory.getCursorStack().isEmpty()))
			return false;
		InventorySorter sorter = new InventorySorter(handler, focusedSlot);
		SortMode sortMode;
		if (hasShiftDown()) {
			sortMode = MouseWheelie.CONFIG.sort.shiftSort.sortMode;
		} else if (hasControlDown()) {
			sortMode = MouseWheelie.CONFIG.sort.controlSort.sortMode;
		} else {
			sortMode = MouseWheelie.CONFIG.sort.primarySort.sortMode;
		}
		if (sortMode == null) return false;
		sorter.sort(sortMode);
		return true;
	}
}
