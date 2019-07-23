package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.util.inventory.ToolPicker;
import de.siphalor.tweed.client.TweedClothBridge;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class ClientCore implements ClientModInitializer {
	public static final String KEY_BINDING_CATEGORY = "key.categories." + MouseWheelie.MOD_ID;
	public static final FabricKeyBinding SORT_KEY_BINDING = FabricKeyBinding.Builder.create(new Identifier(MouseWheelie.MOD_ID, "sort_inventory"), InputUtil.Type.KEYSYM, -1, KEY_BINDING_CATEGORY).build();
	// TODO
	public static final FabricKeyBinding FILL_INVENTORY_KEY_BINDING = FabricKeyBinding.Builder.create(new Identifier(MouseWheelie.MOD_ID, "fill_inventory"), InputUtil.Type.KEYSYM, 71, KEY_BINDING_CATEGORY).build();

	public static TweedClothBridge tweedClothBridge;

	public static boolean awaitSlotUpdate = false;

	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory(KEY_BINDING_CATEGORY);
		KeyBindingRegistry.INSTANCE.register(SORT_KEY_BINDING);

		ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
			Item item = player.getMainHandStack().getItem();
			if(ClientCore.isTool(item) || ClientCore.isWeapon(item) && Config.holdToolPick.value) {
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
			return ItemStack.EMPTY;
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
			if(equipmentSlot != EquipmentSlot.MAINHAND && equipmentSlot != EquipmentSlot.OFFHAND) {
				if(!player.getEquippedStack(equipmentSlot).isEmpty()) {
					player.setStackInHand(hand, player.getEquippedStack(equipmentSlot));
					player.setEquippedStack(equipmentSlot, stack);
					return ActionResult.SUCCESS;
				}
			}
			return ActionResult.PASS;
		});

		Config.initialize();

		tweedClothBridge = new TweedClothBridge(Config.configFile);
	}

	public static boolean isTool(Item item) {
		return item instanceof ToolItem || item instanceof ShearsItem || FabricToolTags.AXES.contains(item) || FabricToolTags.HOES.contains(item) || FabricToolTags.PICKAXES.contains(item) || FabricToolTags.SHOVELS.contains(item);
	}

	public static boolean isWeapon(Item item) {
		return item instanceof RangedWeaponItem || item instanceof TridentItem || item instanceof SwordItem || FabricToolTags.SWORDS.contains(item);
	}
}
