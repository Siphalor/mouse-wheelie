package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.Core;
import de.siphalor.mousewheelie.util.IContainerScreen;
import de.siphalor.mousewheelie.util.SortMode;
import javafx.util.Pair;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mixin(ContainerScreen.class)
public abstract class MixinContainerScreen extends Screen implements IContainerScreen {
	protected MixinContainerScreen(TextComponent textComponent_1) {
		super(textComponent_1);
	}

	@Shadow protected abstract Slot getSlotAt(double double_1, double double_2);

	@Shadow protected abstract void onMouseClick(Slot slot_1, int int_1, int int_2, SlotActionType slotActionType_1);

	@Shadow @Final protected Container container;

	@Shadow @Final protected PlayerInventory playerInventory;

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
        	Slot hoveredSlot = getSlotAt(x, y);
        	if(hoveredSlot == null)
        		return;
        	if(playerInventory.player.abilities.creativeMode && !hoveredSlot.getStack().isEmpty())
        		return;
			 mouseWheelie_combineStacks(hoveredSlot.inventory);
			 mouseWheelie_sort(hoveredSlot.inventory, hasShiftDown() ? SortMode.QUANTITY : SortMode.ALPHABETICAL);
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

	private void mouseWheelie_combineStacks(Inventory inventory) {
		List<Slot> inventorySlots = container.slotList.stream().filter(slot -> {
			ItemStack stack = slot.getStack();
			return mouseWheelie_isSlotValidFor(inventory, slot) && !stack.isEmpty() && stack.getItem().getMaxAmount() > stack.getAmount();
		}).collect(Collectors.toList());
		ItemStack stack;
		for(int i = inventorySlots.size() - 1; i >= 0; i--) {
			stack = inventorySlots.get(i).getStack().copy();
			CompoundTag tag = stack.getTag();
			int stackSize = stack.getAmount();
			if(stackSize >= stack.getItem().getMaxAmount()) continue;
			onMouseClick(inventorySlots.get(i), -1, 0, SlotActionType.PICKUP);
			for(int j = 0; j < i; j++) {
				ItemStack test = inventorySlots.get(j).getStack();
				if(stack.getItem() == test.getItem() && ItemStack.areTagsEqual(stack, test)) {
					stackSize -= test.getItem().getMaxAmount() - test.getAmount();
					onMouseClick(inventorySlots.get(j), -1, 0, SlotActionType.PICKUP);
					if(stackSize <= 0) break;
				}
			}
			if(stackSize > 0) {
				onMouseClick(inventorySlots.get(i), -1, 0, SlotActionType.PICKUP);
			}
		}
	}

	private void mouseWheelie_sort(Inventory inventory, SortMode sortMode) {
        List<Slot> inventorySlots = container.slotList.stream().filter(slot -> mouseWheelie_isSlotValidFor(inventory, slot)).collect(Collectors.toList());
        List<Integer> sortIds = IntStream.range(0, inventorySlots.size()).boxed().collect(Collectors.toList());
        switch(sortMode) {
			case ALPHABETICAL:
				 List<String> strings = sortIds.stream().map(id -> {
					 ItemStack itemStack = inventorySlots.get(id).getStack();
					 if (itemStack.isEmpty()) return "";
					 return I18n.translate(itemStack.getTranslationKey());
				 }).collect(Collectors.toList());
				 sortIds.sort((o1, o2) -> {
					 if (strings.get(o1).equals("")) {
						  if (strings.get(o2).equals(""))
							  return 0;
						  return 1;
					 }
					 if (strings.get(o2).equals("")) return -1;
					 int comp = strings.get(o1).compareToIgnoreCase(strings.get(o2));
					 if (comp == 0) {
						  return Integer.compare(inventorySlots.get(o2).getStack().getAmount(), inventorySlots.get(o1).getStack().getAmount());
					 }
					 return comp;
				 });
				 break;
			case QUANTITY:
				HashMap<Pair<Item, CompoundTag>, Integer> itemToAmountMap = new HashMap<>();
				for(Slot slot : inventorySlots) {
                    ItemStack stack = slot.getStack().copy();
                    if(stack.isEmpty()) continue;
                    Pair<Item, CompoundTag> key = new Pair<>(stack.getItem(), stack.getOrCreateTag());
                    if(!itemToAmountMap.containsKey(key)) {
                    	itemToAmountMap.put(key, slot.getStack().getAmount());
					} else {
                    	itemToAmountMap.replace(key, itemToAmountMap.get(key) + slot.getStack().getAmount());
					}
				}
				sortIds.sort((o1, o2) -> {
					ItemStack stack = inventorySlots.get(o1).getStack();
					ItemStack stack2 = inventorySlots.get(o2).getStack();
					if(stack.isEmpty()) {
						return stack2.isEmpty() ? 0 : 1;
					}
					if(stack2.isEmpty()) {
						return -1;
					}
					Integer a = itemToAmountMap.get(new Pair<>(stack.getItem(), stack.getOrCreateTag()));
					Integer a2 = itemToAmountMap.get(new Pair<>(stack2.getItem(), stack2.getOrCreateTag()));
					return Integer.compare(a2, a);
				});
		}
        BitSet done = new BitSet(sortIds.size());
        for(int i = 0; i < sortIds.size(); i++) {
        	if(done.get(i)) continue;
        	if(inventorySlots.get(i).getStack().isEmpty()) continue;
        	if(sortIds.indexOf(i) == i) {
        		done.set(i);
        		continue;
			}
        	int currentId = i;
        	onMouseClick(inventorySlots.get(currentId), -1, 0, SlotActionType.PICKUP);
        	while(!done.get(sortIds.indexOf(currentId))) {
        		currentId = sortIds.indexOf(currentId);
        		onMouseClick(inventorySlots.get(currentId), -1, 0, SlotActionType.PICKUP);
				done.set(currentId);
			}
		}
	}

	private boolean mouseWheelie_isSlotValidFor(Inventory inventory, Slot slot) {
		return inventory == slot.inventory && slot.canInsert(ItemStack.EMPTY);
	}
}
