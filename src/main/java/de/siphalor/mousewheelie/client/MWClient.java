/*
 * Copyright 2020 Siphalor and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.coat.screen.ConfigScreen;
import de.siphalor.coat.util.EnumeratedMaterial;
import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.MWFeature;
import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.StackPicker;
import de.siphalor.mousewheelie.client.inventory.ToolPicker;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.inventory.view.InventoryView;
import de.siphalor.mousewheelie.client.inventory.view.InventoryViewEntry;
import de.siphalor.mousewheelie.client.inventory.view.InventoryViewLocation;
import de.siphalor.mousewheelie.client.keybinding.*;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.network.MWClientNetworking;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.ScrollAction;
import de.siphalor.mousewheelie.client.util.inject.IContainerScreen;
import de.siphalor.mousewheelie.client.util.inject.IScrollableRecipeBook;
import de.siphalor.mousewheelie.client.util.inject.ISpecialScrollableScreen;
import de.siphalor.tweed5.coat.bridge.api.ConfigScreenCreateParams;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatMappers;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//- import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//- import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
//- import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

//- import static de.siphalor.mousewheelie.MouseWheelie.MOD_ID;
import static de.siphalor.mousewheelie.MouseWheelie.createId;
import static de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension.presetValue;

@Environment(EnvType.CLIENT)
@SuppressWarnings("WeakerAccess")
public class MWClient implements ClientModInitializer {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	//# if MC_VERSION_NUMBER >= 12109
	public static final KeyMapping.Category KEY_BINDING_CATEGORY = new KeyMapping.Category(createId("main"));
	//# else
	//- public static final String KEY_BINDING_CATEGORY = "key.category." + MouseWheelie.MOD_ID + ".main";
	//# end

	public static final KeyMapping OPEN_CONFIG_SCREEN = new OpenConfigScreenKeybinding(
			"open_config_screen",
			InputConstants.UNKNOWN,
			KEY_BINDING_CATEGORY
	);
	public static final KeyMapping SORT_KEY_BINDING = new SortKeyBinding(
			"sort_inventory",
			InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE),
			KEY_BINDING_CATEGORY
	);
	public static final KeyMapping SCROLL_UP_KEY_BINDING = new ScrollKeyBinding(
			"scroll_up",
			KEY_BINDING_CATEGORY,
			false
	);
	public static final KeyMapping SCROLL_DOWN_KEY_BINDING = new ScrollKeyBinding(
			"scroll_down",
			KEY_BINDING_CATEGORY,
			true
	);
	public static final KeyMapping PICK_TOOL_KEY_BINDING = new PickToolKeyBinding(
			"pick_tool",
			InputConstants.UNKNOWN,
			KEY_BINDING_CATEGORY
	);
	public static final ActionModifierKeybinding WHOLE_STACK_MODIFIER = new ActionModifierKeybinding(
			"whole_stack_modifier",
			getKey(InputConstants.KEY_LSHIFT),
			KEY_BINDING_CATEGORY
	);
	public static final ActionModifierKeybinding ALL_OF_KIND_MODIFIER = new ActionModifierKeybinding(
			"all_of_kind_modifier",
			getKey(InputConstants.KEY_LCONTROL),
			KEY_BINDING_CATEGORY
	);
	public static final ActionModifierKeybinding DROP_MODIFIER = new ActionModifierKeybinding(
			"drop_modifier",
			getKey(InputConstants.KEY_LALT),
			KEY_BINDING_CATEGORY
	);
	public static final ActionModifierKeybinding DEPOSIT_MODIFIER = new ActionModifierKeybinding(
			"deposit_modifier",
			getKey(InputConstants.KEY_SPACE),
			KEY_BINDING_CATEGORY
	);
	public static final ActionModifierKeybinding RESTOCK_MODIFIER = new ActionModifierKeybinding(
			"restock_modifier",
			getKey(InputConstants.KEY_SPACE),
			KEY_BINDING_CATEGORY
	);

	public static int lastUpdatedSlot = -1;

	@Override
	public void onInitializeClient() {
		registerKeyMapping(OPEN_CONFIG_SCREEN);
		registerKeyMapping(SORT_KEY_BINDING);
		registerKeyMapping(SCROLL_UP_KEY_BINDING);
		registerKeyMapping(SCROLL_DOWN_KEY_BINDING);
		registerKeyMapping(PICK_TOOL_KEY_BINDING);

		registerKeyMapping(WHOLE_STACK_MODIFIER);
		registerKeyMapping(ALL_OF_KIND_MODIFIER);
		registerKeyMapping(DROP_MODIFIER);
		registerKeyMapping(DEPOSIT_MODIFIER);
		registerKeyMapping(RESTOCK_MODIFIER);

		//# if MC_VERSION_NUMBER < 12104
		//- ClientPickBlockGatherCallback.EVENT.register(MWClient::triggerPick);
		//# end

		MWClientNetworking.setup();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			CreativeSearchOrder.refreshItemSearchPositionLookup();
			updateTickRate();
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			MouseWheelie.endFeatureSession();
		});
	}

	private static InputConstants.Key getKey(int code) {
		//# if MC_VERSION_NUMBER >= 260300
		return InputConstants.Type.KEYBOARD.getOrCreate(code);
		//# else
		//- return InputConstants.Type.KEYSYM.getOrCreate(code);
		//# end
	}

	//# if MC_VERSION_NUMBER >= 260100
	private static void registerKeyMapping(KeyMapping keyMapping) {
		KeyMappingHelper.registerKeyMapping(keyMapping);
	}
	//# else
	//- private static void registerKeyMapping(KeyMapping keyMapping) {
	//- 	KeyBindingHelper.registerKeyBinding(keyMapping);
	//- }
	//# end

	public static boolean isTool(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12103
		return stack.has(DataComponents.TOOL);
		//# else
		//- // TODO: reimplement Fapi tool tags
		//- return stack.getItem() instanceof TieredItem || stack.getItem() instanceof ShearsItem;
		//# end
	}

	public static boolean isWeapon(ItemStack stack) {
		//# if MC_VERSION_NUMBER >= 12103
		return stack.getItem() instanceof ProjectileWeaponItem
				|| stack.getItem() instanceof TridentItem
				|| stack.is(ItemTags.SHARP_WEAPON_ENCHANTABLE);
		//# else
		//- Item item = stack.getItem();
		//- return item instanceof ProjectileWeaponItem || item instanceof TridentItem || item instanceof SwordItem;
		//# end
	}

	public static double getMouseX() {
		return CLIENT.mouseHandler.xpos() * (double) CLIENT.getWindow().getGuiScaledWidth() / (double) CLIENT.getWindow().getScreenWidth();
	}

	public static double getMouseY() {
		return CLIENT.mouseHandler.ypos() * (double) CLIENT.getWindow().getGuiScaledHeight() / (double) CLIENT.getWindow().getScreenHeight();
	}

	public static boolean isScrollModeToggled() {
		//# if MC_VERSION_NUMBER >= 12109
		return Minecraft.getInstance().hasAltDown();
		//# else
		//- return Screen.hasAltDown();
		//# end
	}

	public static void onFeatureSetReduced() {
		MutableComponent message = Component.translatable("mousewheelie.features.reduced-by-server");
		Component[] disabledFeatures = EnumSet.complementOf(MouseWheelie.enabledFeatures)
				.stream()
				.map(MWFeature::getComponent)
				.toArray(Component[]::new);
		for (int i = 0; i < disabledFeatures.length; i++) {
			message.append(disabledFeatures[i]);
			if (i < disabledFeatures.length - 1) {
				message.append(", ");
			}
		}

		//# if MC_VERSION_NUMBER >= 260100
		Minecraft.getInstance().player.sendOverlayMessage(message);
		//# else
		//- Minecraft.getInstance().player.displayClientMessage(message, false);
		//# end
	}

	public static void onConfigChanged() {
		CreativeSearchOrder.refreshItemSearchPositionLookup();
		updateTickRate();
	}

	private static void updateTickRate() {
		if (isOnLocalServer()) {
			InteractionManager.setTickRate(MouseWheelie.config.general.integratedInteractionRate);
		} else {
			InteractionManager.setTickRate(MouseWheelie.config.general.interactionRate);
		}
	}

	public static boolean isOnLocalServer() {
		return CLIENT.getSingleplayerServer() != null;
	}

	public static boolean triggerScroll(double mouseX, double mouseY, double scrollY) {
		double scrollAmount = scrollY * CLIENT.options.mouseWheelSensitivity().get();
		ScrollAction result;
		Screen openScreen = getOpenScreen();
		if (openScreen instanceof ISpecialScrollableScreen) {
			result = ((ISpecialScrollableScreen) openScreen).mouseWheelie_onMouseScrolledSpecial(mouseX, mouseY, scrollAmount);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		if (openScreen instanceof IContainerScreen) {
			result = ((IContainerScreen) openScreen).mouseWheelie_onMouseScroll(mouseX, mouseY, scrollY);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		if (openScreen instanceof IScrollableRecipeBook) {
			result = ((IScrollableRecipeBook) openScreen).mouseWheelie_onMouseScrollRecipeBook(mouseX, mouseY, scrollY);
			if (result.cancelsCustomActions()) {
				return result.cancelsAllActions();
			}
		}
		return false;
	}

	//# if MC_VERSION_NUMBER >= 12104
	public static boolean triggerPick(Player player, HitResult hitResult) {
	//# else
	//- public static ItemStack triggerPick(Player player, HitResult hitResult) {
	//# end
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();
		//# if MC_VERSION_NUMBER < 12104
		//- int index = -1;
		//# end
		if (MouseWheelie.config.toolPicking.holdTool && (isTool(stack) || isWeapon(stack))) {
			ToolPicker toolPicker = new ToolPicker(player.getInventory());
			if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult) {
				BlockState blockState = player.level().getBlockState(((BlockHitResult) hitResult).getBlockPos());
				//# if MC_VERSION_NUMBER >= 12104
				if (toolPicker.pickToolFor(blockState)) {
					return true;
				}
				//# else
				//- index = toolPicker.findToolFor(blockState);
				//# end
			//# if MC_VERSION_NUMBER >= 12104
			} else if (toolPicker.pickWeapon()) {
				return true;
			//# else
			//- } else {
			//- 	index = toolPicker.findWeapon();
			//# end
			}
		}
		if (MouseWheelie.config.toolPicking.holdBlock && item instanceof BlockItem && hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult) {
			BlockState blockState = player.level().getBlockState(((BlockHitResult) hitResult).getBlockPos());
			if (blockState.getBlock() == ((BlockItem) item).getBlock()) {
				ToolPicker toolPicker = new ToolPicker(player.getInventory());
				//# if MC_VERSION_NUMBER >= 12104
				return toolPicker.pickToolFor(blockState);
				//# else
				//- index = toolPicker.findToolFor(blockState);
				//# end
			}
		}
		//# if MC_VERSION_NUMBER >= 12103
		if (MouseWheelie.config.general.pickFromBundles && hitResult instanceof BlockHitResult) {
			BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
			BlockState blockState = player.level().getBlockState(blockPos);
			//# if MC_VERSION_NUMBER >= 12104
			ItemStack referenceStack = blockState.getCloneItemStack(player.level(), blockPos, false);
			//# else
			//- ItemStack referenceStack = blockState.getBlock().getCloneItemStack(player.level(), blockPos, blockState);
			//# end

			InventoryView inventoryView = InventoryView.appendingBundles(
					InventoryView.ofContainerRange(player.getInventory(), 0, Inventory.INVENTORY_SIZE)
			);
			for (InventoryViewEntry entry : inventoryView) {
				if (entry.getStack().getItem() == referenceStack.getItem()) {
					if (entry.getLocation() instanceof InventoryViewLocation.Bundle) {
						StackPicker.Options stackPickerOptions = new StackPicker.Options(
								StackPicker.TargetMode.PREFER_EMPTY_HOTBAR_SLOTS,
								true
						);
						if (new StackPicker(player).pickFromBundleLocation(
								((InventoryViewLocation.Bundle) entry.getLocation()),
								stackPickerOptions
						)) {
							break;
						}
					} else {
						break;
					}
				}
			}
		}
		//# end
		//# if MC_VERSION_NUMBER >= 12104
		return false;
		//# else
		//- return index == -1 || index == player.getInventory().selected ? ItemStack.EMPTY : player.getInventory().getItem(index);
		//# end
	}

	public static ConfigScreen createConfigScreen() {
		TweedCoatBridgeExtension coatBridge = MouseWheelie.configContainerHelper.configContainer().extension(TweedCoatBridgeExtension.class)
				.orElseThrow(() -> new IllegalStateException("Failed to get TweedCoatBridgeExtension"));

		Arrays.asList(
				TweedCoatMappers.booleanCheckboxMapper(),
				TweedCoatMappers.integerTextMapper(),
				TweedCoatMappers.enumCycleButtonMapper(),
				TweedCoatMappers.enumeratedMaterialCycleButtonMapper(SortMode.class, new EnumeratedMaterial<>() {
					@Override
					public SortMode[] values() {
						return SortMode.getAll().toArray(new SortMode[0]);
					}

					@Override
					public Component asText(SortMode sortMode) {
						return Component.translatable("mousewheelie.sortmode." + sortMode.name().toLowerCase(Locale.ROOT));
					}
				}),
				TweedCoatMappers.compoundCategoryMapper()
		).forEach(coatBridge::addMapper);

		MWConfig defaultValue = MouseWheelie.configContainerHelper.configContainer().rootEntry()
				.call(presetValue(PresetsExtension.DEFAULT_PRESET_NAME));

		return coatBridge.createConfigScreen(ConfigScreenCreateParams.<MWConfig>builder()
				.rootEntry(MouseWheelie.configContainerHelper.configContainer().rootEntry())
				.currentValue(MouseWheelie.globalConfig)
				.defaultValue(defaultValue)
				.title(Component.translatable("mousewheelie.config"))
				.translationKeyPrefix("mousewheelie.config")
				.saveHandler(value -> {
					MouseWheelie.updateConfig(value);
					MouseWheelie.configContainerHelper.writeConfigInConfigDirectory(value);
					MWClient.onConfigChanged();
				})
				.build());
	}

	public static Screen getOpenScreen() {
		//# if MC_VERSION_NUMBER >= 260200
		return CLIENT.gui.screen();
		//# else
		//- return CLIENT.screen;
		//# end
	}

	public static void openScreen(Screen screen) {
		//# if MC_VERSION_NUMBER >= 260200
		CLIENT.gui.setScreen(screen);
		//# else
		//- CLIENT.setScreen(screen);
		//# end
	}
}
