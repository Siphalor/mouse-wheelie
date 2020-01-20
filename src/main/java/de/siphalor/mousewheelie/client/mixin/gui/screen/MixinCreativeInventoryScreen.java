package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.Config;
import de.siphalor.mousewheelie.client.compat.FabricCreativeGuiHelper;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import de.siphalor.mousewheelie.client.util.accessors.ISpecialScrollableScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class MixinCreativeInventoryScreen extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeContainer> implements ISpecialScrollableScreen, IContainerScreen {

	@Shadow private static int selectedTab;

	@Shadow protected abstract void setSelectedTab(ItemGroup itemGroup_1);

	@Shadow private List<Slot> slots;

	@Shadow protected abstract void onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType);

	@Shadow private Slot deleteItemSlot;

	public MixinCreativeInventoryScreen(CreativeInventoryScreen.CreativeContainer container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
		super(container_1, playerInventory_1, textComponent_1);
	}

	@Override
	public boolean mouseWheelie_onMouseScrolledSpecial(double mouseX, double mouseY, double scrollAmount) {
		// Exact box matching:
		//if(mouseX >= this.left && mouseX < this.left + this.width && ((mouseY >= this.top - 28 && mouseY < this.top + 4) || (mouseY >= this.top + this.height - 4 && mouseY < this.top + this.height + 28))) {
		// Rough matching:
		if(mouseY < this.top + 4 || mouseY >= this.top + this.containerHeight - 4) {
			if(FabricLoader.getInstance().isModLoaded("fabric") || FabricLoader.getInstance().isModLoaded("fabric-item-groups")) {
				FabricCreativeGuiHelper helper = new FabricCreativeGuiHelper((CreativeInventoryScreen)(Object) this);
				int newIndex = MathHelper.clamp(selectedTab + (int) Math.round(scrollAmount * Config.scrollFactor.value), 0, ItemGroup.GROUPS.length - 1);
				int newPage = helper.getPageForTabIndex(newIndex);
				if(newPage < helper.getCurrentPage())
					helper.previousPage();
				if(newPage > helper.getCurrentPage())
					helper.nextPage();
				setSelectedTab(ItemGroup.GROUPS[newIndex]);
			} else
				setSelectedTab(ItemGroup.GROUPS[MathHelper.clamp((int) (selectedTab + Math.round(scrollAmount * Config.scrollFactor.value)), 0, ItemGroup.GROUPS.length - 1)]);
            return true;
		}

		if(selectedTab != ItemGroup.INVENTORY.getIndex()) {
			Slot slot = this.mouseWheelie_getSlotAt(mouseX, mouseY);
			if (slot != null) {
				int slotId = ((ISlot) slot).mouseWheelie_getInvSlot();
				boolean scrollUp = scrollAmount < 0;
				ItemStack slotStack = slot.getStack();
				Slot tempDelSlot = container.slotList.get(slotStack.getItem() == container.slotList.get(0).getStack().getItem() ? 1 : 0);
				if (slot.inventory == playerInventory) {
					if (scrollUp) {
						if (!slotStack.isEmpty()) {
							if (hasControlDown()) {
								for (Slot testSlot : container.slotList) {
									if (testSlot.inventory == playerInventory && (hasShiftDown() || testSlot.getStack().getItem() == slotStack.getItem()))
										onMouseClick(testSlot, ((ISlot) testSlot).mouseWheelie_getInvSlot(), 0, SlotActionType.QUICK_MOVE);
								}
							} else if (hasShiftDown()) {
								onMouseClick(slot, slotId, 0, SlotActionType.QUICK_MOVE);
							} else {
								onMouseClick(slot, slotId, 0, SlotActionType.PICKUP);
								onMouseClick(tempDelSlot, 0, 1, SlotActionType.PICKUP);
								onMouseClick(slot, slotId, 0, SlotActionType.PICKUP);
							}
						}
					} else {
						onMouseClick(slot, slotId, 2, SlotActionType.CLONE);
						onMouseClick(slot, slotId, hasShiftDown() ? 0 : 1, SlotActionType.PICKUP);
						onMouseClick(tempDelSlot, 0, 0, SlotActionType.PICKUP);
					}
				} else {
					if (!slotStack.isEmpty()) {
						for (Slot testSlot : container.slotList) {
							if (testSlot.inventory == playerInventory) {
								ItemStack testStack = testSlot.getStack();
								if(testStack.isEmpty()) continue;
								if (scrollUp && testStack.getItem() == slotStack.getItem()) {
									if (hasShiftDown() || hasControlDown()) {
										onMouseClick(testSlot, ((ISlot) testSlot).mouseWheelie_getInvSlot(), 0, SlotActionType.QUICK_MOVE);
										if (!hasControlDown())
											return true;
									} else {
										int testSlotId = ((ISlot) testSlot).mouseWheelie_getInvSlot();
										onMouseClick(testSlot, testSlotId, 0, SlotActionType.PICKUP);
										onMouseClick(testSlot, testSlotId, 1, SlotActionType.PICKUP);
										onMouseClick(testSlot, testSlotId, 0, SlotActionType.QUICK_MOVE);
										onMouseClick(testSlot, testSlotId, 0, SlotActionType.PICKUP);
										return true;
									}
								} else if (Container.canStacksCombine(slotStack, testStack) && testStack.getCount() < testStack.getMaxCount()) {
									if (hasShiftDown()) {
										boolean wasEmpty = testSlot.getStack().isEmpty();
										onMouseClick(slot, slotId, 0, SlotActionType.CLONE);
										onMouseClick(testSlot, ((ISlot) testSlot).mouseWheelie_getInvSlot(), 0, SlotActionType.PICKUP);
										if(!wasEmpty)
											onMouseClick(slot, slotId, 0, SlotActionType.PICKUP);
									} else {
										onMouseClick(slot, slotId, 0, SlotActionType.PICKUP);
										onMouseClick(testSlot, ((ISlot) testSlot).mouseWheelie_getInvSlot(), 0, SlotActionType.PICKUP);
									}
									return true;
								}
							}
						}
					}
				}
			}
			return true;
		}
		return false;
	}
}
