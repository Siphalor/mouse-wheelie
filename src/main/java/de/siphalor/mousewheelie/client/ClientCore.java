package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.Core;
import de.siphalor.tweed.client.TweedClothBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class ClientCore implements ClientModInitializer {
	public static final String KEY_BINDING_CATEGORY = "key.categories." + Core.MOD_ID;
	public static final FabricKeyBinding SORT_KEY_BINDING = FabricKeyBinding.Builder.create(new Identifier(Core.MOD_ID, "sort_inventory"), InputUtil.Type.KEYSYM, -1, KEY_BINDING_CATEGORY).build();
	// TODO
	public static final FabricKeyBinding FILL_INVENTORY_KEY_BINDING = FabricKeyBinding.Builder.create(new Identifier(Core.MOD_ID, "fill_inventory"), InputUtil.Type.KEYSYM, 71, KEY_BINDING_CATEGORY).build();

	public static TweedClothBridge tweedClothBridge;

	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory(KEY_BINDING_CATEGORY);
		KeyBindingRegistry.INSTANCE.register(SORT_KEY_BINDING);

		Config.initialize();

		tweedClothBridge = new TweedClothBridge(Config.configFile);
	}
}
