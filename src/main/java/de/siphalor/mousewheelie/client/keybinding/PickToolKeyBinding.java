package de.siphalor.mousewheelie.client.keybinding;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.ListeningKeyBinding;
import de.siphalor.mousewheelie.client.util.inventory.ToolPicker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class PickToolKeyBinding extends AmecsKeyBinding implements ListeningKeyBinding {
	public PickToolKeyBinding(Identifier id, InputUtil.Type type, int code, String category, KeyModifiers defaultModifiers) {
		super(id, type, code, category, defaultModifiers);
	}

	@Override
	public void onPressed() {
		MinecraftClient client = MinecraftClient.getInstance();
		HitResult hitResult = client.hitResult;
		ToolPicker toolPicker = new ToolPicker(client.player.inventory);
		if(hitResult.getType() == HitResult.Type.BLOCK) {
			toolPicker.pickToolFor(client.world.getBlockState(new BlockPos(hitResult.getPos())));
		} else {
			toolPicker.pickWeapon();
		}
	}

	@Override
	public int compareTo(KeyBinding o) {
		return method_1430(o);
	}
}
