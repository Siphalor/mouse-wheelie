package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.inventory.sort.InventorySorter;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("WeakerAccess")
@Mixin(ContainerScreen.class)
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
	protected Container container;

	@Shadow
	@Final
	protected PlayerInventory playerInventory;

	@Shadow
	protected Slot focusedSlot;

	@Inject(method = "keyPressed", at = @At(value = "RETURN", ordinal = 1))
	public void onKeyPressed(int key, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		//noinspection ConstantConditions
		if (this.minecraft.options.keySwapHands.matchesKey(key, scanCode) && focusedSlot != null) {
			boolean putBack = false;
			Slot swapSlot = container.slots.stream().filter(slot -> slot.inventory == playerInventory && ((ISlot) slot).mouseWheelie_getInvSlot() == playerInventory.selectedSlot).findAny().orElse(null);
			if (swapSlot == null) return;
			ItemStack swapStack = playerInventory.getCursorStack().copy();
			ItemStack offHandStack = playerInventory.offHand.get(0).copy();
			if (playerInventory.getCursorStack().isEmpty()) {
				putBack = true;
				if (!focusedSlot.getStack().isEmpty()) {
					swapStack = focusedSlot.getStack().copy();
					InteractionManager.pushClickEvent(container.syncId, focusedSlot.id, 0, SlotActionType.PICKUP);
				} else if (offHandStack.isEmpty()) {
					return;
				}
			}
			InteractionManager.pushClickEvent(container.syncId, swapSlot.id, 0, SlotActionType.PICKUP);
			InteractionManager.push(new InteractionManager.PacketEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, Direction.DOWN)));
			InteractionManager.pushClickEvent(container.syncId, swapSlot.id, 0, SlotActionType.PICKUP);
			if (putBack) {
				InteractionManager.pushClickEvent(container.syncId, focusedSlot.id, 0, SlotActionType.PICKUP);
			}
			ItemStack finalSwapStack = swapStack;
			boolean finalPutBack = putBack;
			// Fix the display up since swapping items doesn't have a confirm packet so we have to trigger the click event too quick afterwards
			InteractionManager.push(() -> {
				playerInventory.offHand.set(0, finalSwapStack);
				if (finalPutBack) {
					focusedSlot.setStack(offHandStack);
					playerInventory.setCursorStack(ItemStack.EMPTY);
				} else {
					playerInventory.setCursorStack(offHandStack);
				}
				return 1;
			});
		}
	}

	@Inject(method = "mouseDragged", at = @At("RETURN"))
	public void onMouseDragged(double x2, double y2, int button, double x1, double y1, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (button == 0) {
			Slot hoveredSlot = getSlotAt(x2, y2);
			if (hoveredSlot != null) {
				if (hasAltDown()) {
					onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.THROW);
				} else if (hasShiftDown()) {
					onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.QUICK_MOVE);
				} else if (hasControlDown()) {
					// noinspection ConstantConditions
					new ContainerScreenHelper<>((ContainerScreen<?>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType)).sendAllOfAKind(hoveredSlot);
				}
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void onMouseClick(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (button == 0) {
			if (hasAltDown()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.THROW);
					callbackInfoReturnable.setReturnValue(true);
				}
			} else if (hasControlDown()) {
				Slot hoveredSlot = getSlotAt(x, y);
				if (hoveredSlot != null) {
					if (hasShiftDown()) {
						//noinspection ConstantConditions
						new ContainerScreenHelper<>((ContainerScreen<?>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType)).sendAllFrom(hoveredSlot);
					} else {
						new ContainerScreenHelper<>((ContainerScreen<?>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType)).sendAllOfAKind(hoveredSlot);
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
	public boolean mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		if (Config.enableItemScrolling.value) {
			if (hasAltDown()) return false;
			Slot hoveredSlot = getSlotAt(mouseX, mouseY);
			if (hoveredSlot == null)
				return false;
			if (hoveredSlot.getStack().isEmpty())
				return false;

			//noinspection ConstantConditions
			if (scrollAmount < 0 && (Object) this instanceof InventoryScreen) {
				EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(hoveredSlot.getStack());
				if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
					InteractionManager.pushClickEvent(container.syncId, hoveredSlot.id, 0, SlotActionType.PICKUP);
					InteractionManager.pushClickEvent(container.syncId, 8 - equipmentSlot.getEntitySlotId(), 0, SlotActionType.PICKUP);
					InteractionManager.pushClickEvent(container.syncId, hoveredSlot.id, 0, SlotActionType.PICKUP);
					return true;
				}
			}

			//noinspection ConstantConditions
			ContainerScreenHelper<?> containerScreenHelper = new ContainerScreenHelper<>((ContainerScreen<?>) (Object) this, (slot, data, slotActionType) -> onMouseClick(slot, -1, data, slotActionType));
			containerScreenHelper.scroll(hoveredSlot, scrollAmount < 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseWheelie_triggerSort() {
		if (focusedSlot == null)
			return false;
		if (playerInventory.player.abilities.creativeMode && (!focusedSlot.getStack().isEmpty() == playerInventory.getCursorStack().isEmpty()))
			return false;
		InventorySorter sorter = new InventorySorter(container, focusedSlot);
		SortMode sortMode;
		if (hasShiftDown()) {
			sortMode = Config.shiftSort.value.sortMode;
		} else if (hasControlDown()) {
			sortMode = Config.controlSort.value.sortMode;
		} else {
			sortMode = Config.primarySort.value.sortMode;
		}
		if (sortMode == null) return false;
		sorter.sort(sortMode);
		return true;
	}
}
