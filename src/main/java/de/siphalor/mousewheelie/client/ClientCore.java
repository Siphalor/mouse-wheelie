package de.siphalor.mousewheelie.client;

import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.ToolPicker;
import de.siphalor.mousewheelie.client.keybinding.OpenConfigScreenKeybinding;
import de.siphalor.mousewheelie.client.keybinding.PickToolKeyBinding;
import de.siphalor.mousewheelie.client.keybinding.ScrollKeyBinding;
import de.siphalor.mousewheelie.client.keybinding.SortKeyBinding;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.IScrollableRecipeBook;
import de.siphalor.mousewheelie.client.util.accessors.ISpecialScrollableScreen;
import de.siphalor.tweed.client.TweedClothBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

@SuppressWarnings("WeakerAccess")
public class ClientCore implements ClientModInitializer {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static final String KEY_BINDING_CATEGORY = "key.categories." + MouseWheelie.MOD_ID;

	public static final FabricKeyBinding OPEN_CONFIG_SCREEN = new OpenConfigScreenKeybinding(new Identifier(MouseWheelie.MOD_ID, "open_config_screen"), InputUtil.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, KeyModifiers.NONE);
	public static final FabricKeyBinding SORT_KEY_BINDING = new SortKeyBinding(new Identifier(MouseWheelie.MOD_ID, "sort_inventory"), InputUtil.Type.MOUSE, 2, KEY_BINDING_CATEGORY, KeyModifiers.NONE);
	public static final FabricKeyBinding SCROLL_UP_KEY_BINDING = new ScrollKeyBinding(new Identifier(MouseWheelie.MOD_ID, "scroll_up"), KEY_BINDING_CATEGORY, false);
	public static final FabricKeyBinding SCROLL_DOWN_KEY_BINDING = new ScrollKeyBinding(new Identifier(MouseWheelie.MOD_ID, "scroll_down"), KEY_BINDING_CATEGORY, true);
	public static final FabricKeyBinding PICK_TOOL_KEY_BINDING = new PickToolKeyBinding(new Identifier(MouseWheelie.MOD_ID, "pick_tool"), InputUtil.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, KeyModifiers.NONE);

	public static TweedClothBridge tweedClothBridge;

	public static boolean awaitSlotUpdate = false;

	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory(KEY_BINDING_CATEGORY);
		KeyBindingRegistry.INSTANCE.register(OPEN_CONFIG_SCREEN);
		KeyBindingRegistry.INSTANCE.register(SORT_KEY_BINDING);
		KeyBindingRegistry.INSTANCE.register(SCROLL_UP_KEY_BINDING);
		KeyBindingRegistry.INSTANCE.register(SCROLL_DOWN_KEY_BINDING);
		KeyBindingRegistry.INSTANCE.register(PICK_TOOL_KEY_BINDING);

		ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
			Item item = player.getMainHandStack().getItem();
			if(Config.holdToolPick.value && (ClientCore.isTool(item) || ClientCore.isWeapon(item))) {
				if(result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult) {
					ToolPicker toolPicker = new ToolPicker(player.inventory);
					int index = toolPicker.findToolFor(player.world.getBlockState(((BlockHitResult) result).getBlockPos()));
					return index == -1 ? ItemStack.EMPTY : player.inventory.getInvStack(index);
				} else {
					ToolPicker toolPicker = new ToolPicker(player.inventory);
					int index = toolPicker.findWeapon();
                    return index == -1 ? ItemStack.EMPTY : player.inventory.getInvStack(index);
				}
			}
			if(Config.holdBlockToolPick.value && item instanceof BlockItem && result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult) {
				BlockState blockState = player.world.getBlockState(((BlockHitResult) result).getBlockPos());
				if(blockState.getBlock() == ((BlockItem) item).getBlock()) {
					ToolPicker toolPicker = new ToolPicker(player.inventory);
					int index = toolPicker.findToolFor(blockState);
					return index == -1 ? ItemStack.EMPTY : player.inventory.getInvStack(index);
				}
			}
			return ItemStack.EMPTY;
		});

		Config.initialize();

		if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
			CLIENT.options.load();
		}

		tweedClothBridge = new TweedClothBridge(Config.configFile);
	}

	public static boolean isTool(Item item) {
		return item instanceof ToolItem || item instanceof ShearsItem || FabricToolTags.AXES.contains(item) || FabricToolTags.HOES.contains(item) || FabricToolTags.PICKAXES.contains(item) || FabricToolTags.SHOVELS.contains(item);
	}

	public static boolean isWeapon(Item item) {
		return item instanceof RangedWeaponItem || item instanceof TridentItem || item instanceof SwordItem || FabricToolTags.SWORDS.contains(item);
	}

	public static double getMouseX() {
		return CLIENT.mouse.getX() * (double) CLIENT.window.getScaledWidth() / (double) CLIENT.window.getWidth();
	}

	public static double getMouseY() {
		return CLIENT.mouse.getY() * (double) CLIENT.window.getScaledHeight() / (double) CLIENT.window.getHeight();
	}

	public static boolean triggerScroll(double mouseX, double mouseY, double scrollY) {
		double scrollAmount = scrollY * CLIENT.options.mouseWheelSensitivity;
		if(CLIENT.currentScreen instanceof ISpecialScrollableScreen) {
			if(((ISpecialScrollableScreen) CLIENT.currentScreen).mouseWheelie_onMouseScrolledSpecial(mouseX, mouseY, scrollAmount)) {
				return true;
			}
		}
		if(CLIENT.currentScreen instanceof IContainerScreen) {
			if(((IContainerScreen) CLIENT.currentScreen).mouseWheelie_onMouseScroll(mouseX, mouseY, scrollAmount)) {
				return true;
			}
		}
		if(CLIENT.currentScreen instanceof IScrollableRecipeBook) {
			//noinspection RedundantIfStatement
			if(((IScrollableRecipeBook) CLIENT.currentScreen).mouseWheelie_onMouseScrollRecipeBook(mouseX, mouseY, scrollAmount)) {
				return true;
			}
		}
		return false;
	}
}
