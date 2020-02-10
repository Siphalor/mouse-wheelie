package de.siphalor.mousewheelie.client.mixin.gui.screen;

import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.container.Slot;
import net.minecraft.container.StonecutterContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StonecutterScreen.class)
public abstract class MixinStonecutterScreen extends ContainerScreen<StonecutterContainer> implements IContainerScreen {

	public MixinStonecutterScreen(StonecutterContainer container, PlayerInventory playerInventory, Text name) {
		super(container, playerInventory, name);
	}

	@Override
	public boolean mouseWheelie_onMouseScroll(double mouseX, double mouseY, double scrollAmount) {
		Slot slot = mouseWheelie_getSlotAt(mouseX, mouseY);

		if (slot != null) {
			new ContainerScreenHelper<>((StonecutterScreen) (Object) this, (slot1, data, slotActionType) -> InteractionManager.push(new InteractionManager.CallbackEvent(() -> {
				onMouseClick(slot1, ((ISlot) slot1).mouseWheelie_getInvSlot(), data, slotActionType);
				return 1;
			}))).scroll(slot, scrollAmount < 0);
			return true;
		}

		return false;
	}
}
