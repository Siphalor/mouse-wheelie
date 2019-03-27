package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.Core;
import de.siphalor.mousewheelie.util.IContainerScreen;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerScreen.class)
public abstract class MixinContainerScreen extends Screen implements IContainerScreen {
	protected MixinContainerScreen(TextComponent textComponent_1) {
		super(textComponent_1);
	}

	@Shadow protected abstract Slot getSlotAt(double double_1, double double_2);

	@Shadow protected abstract void onMouseClick(Slot slot_1, int int_1, int int_2, SlotActionType slotActionType_1);

	@Shadow @Final protected Container container;

	public boolean mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		Slot hoveredSlot = getSlotAt(mouseX, mouseY);
		if(hoveredSlot == null)
			return false;
		ItemStack hoveredStack = hoveredSlot.getStack();
		boolean isPlayerSlot = hoveredSlot.inventory instanceof PlayerInventory;
		boolean moveUp = scrollAmount * Core.scrollFactor < 0;
		if((isPlayerSlot && moveUp) || (!isPlayerSlot && !moveUp)) {
			if(hasControlDown()) {
				for(Slot slot : container.slotList) {
					if(slot.inventory == hoveredSlot.inventory) {
						if(slot.getStack().isEqualIgnoreTags(hoveredStack))
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
}
