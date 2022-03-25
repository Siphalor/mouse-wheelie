package de.siphalor.mousewheelie.client.network;

import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public interface ClickEventFactory {
	InteractionManager.InteractionEvent create(Slot slot, int action, SlotActionType slotActionType);
}
