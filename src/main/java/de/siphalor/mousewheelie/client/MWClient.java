package de.siphalor.mousewheelie.client;

import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.inventory.ToolPicker;
import de.siphalor.mousewheelie.client.keybinding.OpenConfigScreenKeybinding;
import de.siphalor.mousewheelie.client.keybinding.PickToolKeyBinding;
import de.siphalor.mousewheelie.client.keybinding.ScrollKeyBinding;
import de.siphalor.mousewheelie.client.keybinding.SortKeyBinding;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.accessors.IContainerScreen;
import de.siphalor.mousewheelie.client.util.accessors.IScrollableRecipeBook;
import de.siphalor.mousewheelie.client.util.accessors.ISpecialScrollableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class MWClient implements ClientModInitializer {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static final String KEY_BINDING_CATEGORY = "key.categories." + MouseWheelie.MOD_ID;

	public static final KeyBinding OPEN_CONFIG_SCREEN = new OpenConfigScreenKeybinding(new Identifier(MouseWheelie.MOD_ID, "open_config_screen"), InputUtil.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final KeyBinding SORT_KEY_BINDING = new SortKeyBinding(new Identifier(MouseWheelie.MOD_ID, "sort_inventory"), InputUtil.Type.MOUSE, 2, KEY_BINDING_CATEGORY, new KeyModifiers());
	public static final KeyBinding SCROLL_UP_KEY_BINDING = new ScrollKeyBinding(new Identifier(MouseWheelie.MOD_ID, "scroll_up"), KEY_BINDING_CATEGORY, false);
	public static final KeyBinding SCROLL_DOWN_KEY_BINDING = new ScrollKeyBinding(new Identifier(MouseWheelie.MOD_ID, "scroll_down"), KEY_BINDING_CATEGORY, true);
	public static final KeyBinding PICK_TOOL_KEY_BINDING = new PickToolKeyBinding(new Identifier(MouseWheelie.MOD_ID, "pick_tool"), InputUtil.Type.KEYSYM, -1, KEY_BINDING_CATEGORY, new KeyModifiers());

	private static Hand refillHand = null;
	public static int lastUpdatedSlot = -1;

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(OPEN_CONFIG_SCREEN);
		KeyBindingHelper.registerKeyBinding(SORT_KEY_BINDING);
		KeyBindingHelper.registerKeyBinding(SCROLL_UP_KEY_BINDING);
		KeyBindingHelper.registerKeyBinding(SCROLL_DOWN_KEY_BINDING);
		KeyBindingHelper.registerKeyBinding(PICK_TOOL_KEY_BINDING);

		ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
			Item item = player.getMainHandStack().getItem();
			int index = -1;
			if (MWConfig.general.holdToolPick && (isTool(item) || isWeapon(item))) {
				ToolPicker toolPicker = new ToolPicker(player.inventory);
				if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult) {
					index = toolPicker.findToolFor(player.world.getBlockState(((BlockHitResult) result).getBlockPos()));
				} else {
					index = toolPicker.findWeapon();
				}
			}
			if (MWConfig.general.holdBlockToolPick && item instanceof BlockItem && result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult) {
				BlockState blockState = player.world.getBlockState(((BlockHitResult) result).getBlockPos());
				if (blockState.getBlock() == ((BlockItem) item).getBlock()) {
					ToolPicker toolPicker = new ToolPicker(player.inventory);
					index = toolPicker.findToolFor(blockState);
				}
			}
			return index == -1 || index == player.inventory.selectedSlot ? ItemStack.EMPTY : player.inventory.getStack(index);
		});
	}

	public static void scheduleRefill(Hand hand, PlayerInventory inventory, ItemStack stack) {
		refillHand = hand;
		SlotRefiller.set(inventory, stack);
	}

	public static boolean performRefill() {
		if (refillHand == null) return false;

		Hand hand = refillHand;
		refillHand = null;
		if (MWConfig.refill.offHand && hand.equals(Hand.OFF_HAND)) {
			InteractionManager.push(new InteractionManager.PacketEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN), triggerType -> triggerType == InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE && MWClient.lastUpdatedSlot >= 36));
		}
		SlotRefiller.refill();
		if (MWConfig.refill.offHand && hand.equals(Hand.OFF_HAND)) {
			InteractionManager.push(new InteractionManager.PacketEvent(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN), triggerType -> triggerType == InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE && MWClient.lastUpdatedSlot >= 36));
		}

		return true;
	}

	public static boolean isTool(Item item) {
		return item instanceof ToolItem || item instanceof ShearsItem || FabricToolTags.AXES.contains(item) || FabricToolTags.HOES.contains(item) || FabricToolTags.PICKAXES.contains(item) || FabricToolTags.SHOVELS.contains(item);
	}

	public static boolean isWeapon(Item item) {
		return item instanceof RangedWeaponItem || item instanceof TridentItem || item instanceof SwordItem || FabricToolTags.SWORDS.contains(item);
	}

	public static double getMouseX() {
		return CLIENT.mouse.getX() * (double) CLIENT.getWindow().getScaledWidth() / (double) CLIENT.getWindow().getWidth();
	}

	public static double getMouseY() {
		return CLIENT.mouse.getY() * (double) CLIENT.getWindow().getScaledHeight() / (double) CLIENT.getWindow().getHeight();
	}

	public static boolean triggerScroll(double mouseX, double mouseY, double scrollY) {
		double scrollAmount = scrollY * CLIENT.options.mouseWheelSensitivity;
		ScrollAction result;
		if (CLIENT.currentScreen instanceof ISpecialScrollableScreen) {
			result = ((ISpecialScrollableScreen) CLIENT.currentScreen).mouseWheelie_onMouseScrolledSpecial(mouseX, mouseY, scrollAmount);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		if (CLIENT.currentScreen instanceof IContainerScreen) {
			result = ((IContainerScreen) CLIENT.currentScreen).mouseWheelie_onMouseScroll(mouseX, mouseY, scrollY);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		if (CLIENT.currentScreen instanceof IScrollableRecipeBook) {
			result = ((IScrollableRecipeBook) CLIENT.currentScreen).mouseWheelie_onMouseScrollRecipeBook(mouseX, mouseY, scrollY);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		return false;
	}
}
