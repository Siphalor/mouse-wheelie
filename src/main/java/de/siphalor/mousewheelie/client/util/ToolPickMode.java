package de.siphalor.mousewheelie.client.util;

import de.siphalor.mousewheelie.client.ClientCore;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

public enum ToolPickMode {
	SHIFT, HOLD_TOOL, NONE;

	public static boolean isTriggered(ToolPickMode mode, PlayerEntity playerEntity) {
        if(mode == SHIFT) return Screen.hasShiftDown();
        if(mode == HOLD_TOOL) {
        	Item item = playerEntity.getMainHandStack().getItem();
        	return ClientCore.isTool(item) || ClientCore.isWeapon(item);
		}
		return false;
	}
}
