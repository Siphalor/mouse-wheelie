package de.siphalor.mousewheelie.client.mixin;

import de.siphalor.mousewheelie.client.util.accessors.ISlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public class MixinSlot implements ISlot {
	@Shadow
	@Final
	private int index;

	@Override
	public int mouseWheelie_getInvSlot() {
		return index;
	}
}
