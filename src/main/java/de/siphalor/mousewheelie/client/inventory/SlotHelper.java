package de.siphalor.mousewheelie.client.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.screen.slot.Slot;

@Environment(EnvType.CLIENT)
public class SlotHelper {

	private SlotHelper() {
	}

	/**
	 * Returns if the given slot is likely a fake slot and doesn't actually have a real inventory.
	 */
	public static boolean isFakeSlot(Slot slot) {
		// The inventory is likely only null if a mod completely overrides it,
		// and if it's not null, and has size 0, it's very very likely to not
		// be a real slot.
		return slot.inventory == null || slot.inventory.size() == 0;
	}

}
