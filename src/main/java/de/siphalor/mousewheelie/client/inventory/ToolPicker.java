package de.siphalor.mousewheelie.client.inventory;

import de.siphalor.mousewheelie.client.MWClient;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.PickFromInventoryC2SPacket;

public class ToolPicker {
	PlayerInventory inventory;

	static int lastToolPickSlot = -1;

	public ToolPicker(PlayerInventory inventory) {
		this.inventory = inventory;
	}

	public int findToolFor(BlockState blockState) {
		float bestBreakSpeed = 1.0F;
		int bestSpeedSlot = -1;
		for (int i = 1; i <= inventory.getInvSize(); i++) {
			int index = (i + lastToolPickSlot) % inventory.getInvSize();
			if (index == inventory.selectedSlot) continue;
			ItemStack stack = inventory.getInvStack(index);
			if (stack.isEffectiveOn(blockState)) {
				lastToolPickSlot = index;
				return index;
			} else {
				float breakSpeed = stack.getMiningSpeed(blockState);
				if (breakSpeed > bestBreakSpeed) {
					bestSpeedSlot = index;
					bestBreakSpeed = breakSpeed;
				}
			}
		}
		if (bestBreakSpeed == -1) {
			ItemStack stack = inventory.getInvStack(inventory.selectedSlot);
			if (stack.isEffectiveOn(blockState) || stack.getMiningSpeed(blockState) > 1.0F)
				return inventory.selectedSlot;
		}
		return bestSpeedSlot;
	}

	public boolean pickToolFor(BlockState blockState) {
		return pick(findToolFor(blockState));
	}

	public int findWeapon() {
		for (int i = 1; i <= inventory.getInvSize(); i++) {
			int index = (i + lastToolPickSlot) % inventory.getInvSize();
			if (index == inventory.selectedSlot) continue;
			if (MWClient.isWeapon(inventory.getInvStack(index).getItem()))
				return index;
		}
		return -1;
	}

	public boolean pickWeapon() {
		return pick(findWeapon());
	}

	private boolean pick(int index) {
		if (index != -1) {
			PickFromInventoryC2SPacket packet = new PickFromInventoryC2SPacket(index);
			ClientSidePacketRegistry.INSTANCE.sendToServer(packet);
			return true;
		}
		return false;
	}
}
