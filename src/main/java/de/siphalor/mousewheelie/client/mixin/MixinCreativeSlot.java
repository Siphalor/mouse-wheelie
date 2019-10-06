package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.minecraft.container.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/client/gui/screen/ingame/CreativeInventoryScreen$CreativeSlot")
public class MixinCreativeSlot implements ISlot {

	@Shadow @Final private Slot slot;

	@Override
	public int mouseWheelie_getInvSlot() {
		return ((ISlot) slot).mouseWheelie_getInvSlot();
	}
}
