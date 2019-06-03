package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.Core;
import de.siphalor.tweed.client.TweedClothBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class ClientCore implements ClientModInitializer {
	public static final String KEY_BINDING_CATEGORY = "key.categories." + Core.MOD_ID;
	public static final FabricKeyBinding SORT_KEY_BINDING = FabricKeyBinding.Builder.create(new Identifier(Core.MOD_ID, "sort_inventory"), InputUtil.Type.KEYSYM, -1, KEY_BINDING_CATEGORY).build();
	public static final FabricKeyBinding TOOL_SELECT_KEY_BINDING = FabricKeyBinding.Builder.create(new Identifier(Core.MOD_ID, "select_tool"), InputUtil.Type.KEYSYM, 341, KEY_BINDING_CATEGORY).build();
	// TODO
	public static final FabricKeyBinding FILL_INVENTORY_KEY_BINDING = FabricKeyBinding.Builder.create(new Identifier(Core.MOD_ID, "fill_inventory"), InputUtil.Type.KEYSYM, 71, KEY_BINDING_CATEGORY).build();

	public static TweedClothBridge tweedClothBridge;

	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory(KEY_BINDING_CATEGORY);
		KeyBindingRegistry.INSTANCE.register(SORT_KEY_BINDING);
		KeyBindingRegistry.INSTANCE.register(TOOL_SELECT_KEY_BINDING);

		ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
			if(TOOL_SELECT_KEY_BINDING.isPressed() && result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult) {
				PlayerInventory inventory = player.inventory;
				for (int i = 0; i < inventory.getInvSize(); i++) {
					if (inventory.getInvStack(i).isEffectiveOn(player.world.getBlockState(((BlockHitResult) result).getBlockPos())))
						return inventory.getInvStack(i);
				}
			}
			return ItemStack.EMPTY;
		});

		Config.initialize();

		tweedClothBridge = new TweedClothBridge(Config.configFile);
	}
}
