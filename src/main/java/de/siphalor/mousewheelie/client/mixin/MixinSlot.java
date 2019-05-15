package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.util.ISlot;
import net.minecraft.container.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public class MixinSlot implements ISlot {
	@Shadow @Final private int invSlot;

	@Override
	public int mouseWheelie_getInvSlot() {
		return invSlot;
	}
}
