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

package de.siphalor.mousewheelie;

import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatAttributes;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.commentloaderextension.api.CommentLoaderExtension;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.fabric.helper.api.DefaultTweedMinecraftWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
//- import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.TweedExtension;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.Attribute;
import de.siphalor.tweed5.weaver.pojoext.presets.api.Preset;
//- import de.siphalor.tweed5.weaver.pojoext.serde.api.EntryReadWriteConfig;
//- import de.siphalor.tweed5.weaver.pojoext.serde.api.ReadWritePojoWeavingProcessor;
//- import de.siphalor.tweed5.weaver.pojoext.serde.api.nullable.AutoNullableReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.validation.api.Validator;
import de.siphalor.tweed5.weaver.pojoext.validation.api.validators.WeavableNumberRangeValidator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@SuppressWarnings({"WeakerAccess", "unused"})
@DefaultTweedMinecraftWeaving
@TweedExtension(CommentLoaderExtension.class)
@TweedExtension(TweedCoatBridgeExtension.class)
@CompoundWeaving(namingFormat = "kebab_case")
@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/green_concrete_powder.png")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MWConfig {
	@Preset(PresetsExtension.DEFAULT_PRESET_NAME)
	public static final MWConfig DEFAULT = new MWConfig(new General(), new Scrolling(), new Sort(), new Refill(), new ToolPicking());

	public General general;
	public Scrolling scrolling;
	public Sort sort;
	public Refill refill;
	public ToolPicking toolPicking;

	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/acacia_log.png")
	public static class General {
		@Validator(value = WeavableNumberRangeValidator.class, config = "1=..")
		public int interactionRate = 10;

		@Validator(value = WeavableNumberRangeValidator.class, config = "1=..")
		public int integratedInteractionRate = 1;

		//# if MC_VERSION_NUMBER < 11904
		//- @AConfigEntry(environment = ConfigEnvironment.UNIVERSAL)
		//- public boolean enableQuickArmorSwapping = true;
		//# end

		public boolean enableDropModifier = true;

		public boolean enableQuickCraft = true;

		public ItemStackUtils.NbtMatchMode itemKindsNbtMatchMode = ItemStackUtils.NbtMatchMode.SOME;

		public enum HotbarScoping {HARD, SOFT, NONE}

		public HotbarScoping hotbarScoping = HotbarScoping.SOFT;

		public boolean betterFastDragging = false;

		public boolean enableBundleDragging = true;
	}

	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/dark_prismarine.png")
	public static class Scrolling {
		public boolean enable = true;
		public boolean invert = false;
		public boolean directionalScrolling = true;
		//# if MC_VERSION_NUMBER >= 12103
		public boolean preferStackSpecialScrollActions = true;
		//# end
		public boolean scrollCreativeMenuItems = true;
		public boolean scrollCreativeMenuTabs = true;
	}

	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/barrel_top.png")
	public static class Sort {
		public SortMode primarySort = SortMode.CREATIVE;
		public SortMode shiftSort = SortMode.QUANTITY;
		public SortMode controlSort = SortMode.ALPHABET;
		public boolean serverAcceleratedSorting = true;
		public boolean optimizeCreativeSearchSort = true;
	}

	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/horn_coral_block.png")
	public static class Refill {
		public boolean enable = true;

		public boolean playSound = true;

		public boolean offHand = true;
		public boolean restoreSelectedSlot = false;

		public boolean itemChanges = true;

		public boolean eat = true;
		public boolean drop = true;
		public boolean use = true;
		public boolean other = true;

		public Rules rules = new Rules();

		@CompoundWeaving
		@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/yellow_terracotta.png")
		public static class Rules {
			public boolean anyBlock = false;
			public boolean itemgroup = false;
			public boolean itemHierarchy = false;
			public boolean blockHierarchy = false;
			public boolean food = false;
			public boolean equalItems = true;
			public boolean equalStacks = true;
		}
	}

	@CompoundWeaving
	@Attribute(key = TweedCoatAttributes.BACKGROUND_TEXTURE, value = "textures/block/coarse_dirt.png")
	public static class ToolPicking {
		public boolean holdTool = true;
		public boolean holdBlock = false;
		public boolean pickFromInventory = true;
	}
}
