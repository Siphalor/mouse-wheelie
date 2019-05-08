package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.Core;
import de.siphalor.mousewheelie.client.ClientFabricCore;
import de.siphalor.mousewheelie.util.IContainerScreen;
import de.siphalor.mousewheelie.util.InventorySorter;
import de.siphalor.mousewheelie.util.SortMode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.text.TextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerScreen.class)
public abstract class MixinContainerScreen extends Screen implements IContainerScreen {
	protected MixinContainerScreen(TextComponent textComponent_1) {
		super(textComponent_1);
	}

	@Shadow protected abstract Slot getSlotAt(double double_1, double double_2);

	@Shadow protected abstract void onMouseClick(Slot slot_1, int int_1, int int_2, SlotActionType slotActionType_1);

	@Shadow @Final protected Container container;

	@Shadow @Final protected PlayerInventory playerInventory;

	@Shadow protected int containerHeight;

	@Shadow private ItemStack touchDragStack;

	@Shadow protected Slot focusedSlot;

	@Shadow private Slot touchDragSlotStart;

	@Inject(method = "keyPressed", at = @At(value = "RETURN", ordinal = 1))
	public void onKeyPressed(int key, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(this.minecraft.options.keySwapHands.matchesKey(key, scanCode)) {
			boolean putBack = false;
			Slot swapSlot = container.slotList.stream().filter(slot -> slot.getStack() == playerInventory.getInvStack(playerInventory.selectedSlot)).findAny().orElse(null);
			if(swapSlot == null) return;
			ItemStack swapStack = touchDragStack.copy();
			ItemStack offHandStack = playerInventory.offHand.get(0).copy();
			if (touchDragStack.isEmpty()) {
				putBack = true;
				if(focusedSlot != null && !focusedSlot.getStack().isEmpty()) {
					swapStack = focusedSlot.getStack().copy();
					Core.pushClickEvent(container.syncId, focusedSlot.id, 0, SlotActionType.PICKUP);
				} else if(offHandStack.isEmpty()) {
					return;
				}
			}
			Core.pushClickEvent(container.syncId, swapSlot.id, 0, SlotActionType.PICKUP);
			Core.push(new Core.PacketEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, Direction.DOWN)));
			Core.pushClickEvent(container.syncId, swapSlot.id, 0, SlotActionType.PICKUP);
			if(putBack) {
				Core.pushClickEvent(container.syncId, focusedSlot.id, 0, SlotActionType.PICKUP);
			}
			ItemStack finalSwapStack = swapStack;
			boolean finalPutBack = putBack;
			// Fix the display up since swapping items doesn't have a confirm packet so we have to trigger the click event too quick afterwards
			Core.push(() -> {
				playerInventory.offHand.set(0, finalSwapStack);
				if(finalPutBack) {
					focusedSlot.setStack(offHandStack);
					touchDragStack = ItemStack.EMPTY;
					playerInventory.setCursorStack(ItemStack.EMPTY);
				} else {
					touchDragStack = offHandStack;
				}
				return true;
			});
		} else if(FabricLoader.getInstance().isModLoaded("fabric")) {
			if(ClientFabricCore.SORT_KEY_BINDING.matchesKey(key, scanCode)) {
				mouseWheelie_triggerSort();
			}
		}
	}

	@Inject(method = "mouseDragged", at = @At("RETURN"))
	public void onMouseDragged(double x2, double y2, int button, double x1, double y1, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if(button == 0 && hasShiftDown()) {
			Slot hoveredSlot = getSlotAt(x2, y2);
			if(hoveredSlot != null)
				onMouseClick(hoveredSlot, hoveredSlot.id, 1, SlotActionType.QUICK_MOVE);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void onMouseClick(double x, double y, int button, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(button == 2) {
        	if(mouseWheelie_triggerSort())
				callbackInfoReturnable.setReturnValue(true);
		}
	}

	public boolean mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		Slot hoveredSlot = getSlotAt(mouseX, mouseY);
		if(hoveredSlot == null)
			return false;
		if(hoveredSlot.getStack().isEmpty())
			return false;
		ItemStack hoveredStack = hoveredSlot.getStack();
		boolean isPlayerSlot = hoveredSlot.inventory instanceof PlayerInventory;
		boolean moveUp = scrollAmount * Core.scrollFactor < 0;
		if((isPlayerSlot && moveUp) || (!isPlayerSlot && !moveUp)) {
			if(!hoveredSlot.canInsert(ItemStack.EMPTY)) {
				onMouseClick(hoveredSlot, hoveredSlot.id, 0, SlotActionType.QUICK_MOVE);
			}
			if(hasControlDown()) {
				ItemStack referenceStack = hoveredStack.copy();
				for(Slot slot : container.slotList) {
					if(slot.inventory == hoveredSlot.inventory) {
						if(slot.getStack().isEqualIgnoreTags(referenceStack))
							onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
					}
				}
			} else if(hasShiftDown()) {
				onMouseClick(hoveredSlot, hoveredSlot.id, 0, SlotActionType.QUICK_MOVE);
			} else {
				mouseWheelie_sendSingleItem(hoveredSlot);
			}
		} else {
			if(hasShiftDown() || hasControlDown()) {
				for(Slot slot : container.slotList) {
					if(slot.inventory == hoveredSlot.inventory) continue;
					if(slot.getStack().isEqualIgnoreTags(hoveredStack)) {
						onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
						if(!hasControlDown())
							break;
					}
				}
			} else {
				Slot moveSlot = null;
				int stackSize = Integer.MAX_VALUE;
				for(Slot slot : container.slotList) {
					if(slot.inventory == hoveredSlot.inventory) continue;
					if(slot.getStack().isEqualIgnoreTags(hoveredStack)) {
						if(slot.getStack().getAmount() < stackSize) {
							stackSize = slot.getStack().getAmount();
							moveSlot = slot;
							if(stackSize == 1) break;
						}
					}
				}
				if(moveSlot != null)
					mouseWheelie_sendSingleItem(moveSlot);
			}
		}
		return true;
	}

	private void mouseWheelie_sendSingleItem(Slot slot) {
		onMouseClick(slot, slot.id, 0, SlotActionType.PICKUP);
		onMouseClick(slot, slot.id, 1, SlotActionType.PICKUP);
		onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
		onMouseClick(slot, slot.id, 0, SlotActionType.PICKUP);
	}

	private boolean mouseWheelie_triggerSort() {
		if(focusedSlot == null)
			return false;
		if(playerInventory.player.abilities.creativeMode && !focusedSlot.getStack().isEmpty())
			return false;
		InventorySorter sorter = new InventorySorter(container, focusedSlot);
		SortMode sortMode;
		if(hasShiftDown()) {
			sortMode = SortMode.QUANTITY;
		} else if(hasControlDown()) {
			sortMode = SortMode.RAWID;
		} else {
			sortMode = SortMode.ALPHABET;
		}
		sorter.sort(sortMode);
		return true;
	}
}
