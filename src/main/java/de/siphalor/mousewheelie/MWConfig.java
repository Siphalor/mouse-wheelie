/*
 * Copyright 2020-2022 Siphalor
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

import com.google.common.base.CaseFormat;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.tweed4.annotated.*;
import de.siphalor.tweed4.config.ConfigEnvironment;
import de.siphalor.tweed4.config.ConfigScope;
import de.siphalor.tweed4.config.constraints.RangeConstraint;
import de.siphalor.tweed4.data.DataList;
import de.siphalor.tweed4.data.DataObject;
import de.siphalor.tweed4.data.DataValue;

@SuppressWarnings({"WeakerAccess", "unused"})
@ATweedConfig(environment = ConfigEnvironment.CLIENT, scope = ConfigScope.SMALLEST, tailors = {"tweed4:lang_json_descriptions", "tweed4:coat"}, casing = CaseFormat.LOWER_HYPHEN)
@AConfigBackground("textures/block/green_concrete_powder.png")
public class MWConfig {
	@AConfigEntry(comment = "General settings")
	public static General general = new General();

	@AConfigBackground("textures/block/acacia_log.png")
	public static class General {
		@AConfigEntry(
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..")
		)
		public int interactionRate = 10;
		@AConfigEntry(
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..")
		)
		public int integratedInteractionRate = 1;

		@AConfigEntry(environment = ConfigEnvironment.UNIVERSAL)
		public boolean enableQuickArmorSwapping = true;

		public boolean enableDropModifier = true;

		public boolean enableQuickCraft = true;

		public ItemStackUtils.NbtMatchMode itemKindsNbtMatchMode = ItemStackUtils.NbtMatchMode.SOME;

		public enum HotbarScoping {HARD, SOFT, NONE}

		public HotbarScoping hotbarScoping = HotbarScoping.SOFT;

		public boolean betterFastDragging = false;

		@AConfigEntry(comment = "Enables dragging bundles while holding right-click to pick up or put out multiple stacks in a single swipe.")
		public boolean enableBundleDragging = true;

		@AConfigListener("interaction-rate")
		public void onReloadInteractionRate() {
			if (!MWClient.isOnLocalServer()) {
				InteractionManager.setTickRate(interactionRate);
			}
		}

		@AConfigListener("integrated-interaction-rate")
		public void onReloadIntegratedInteractionRate() {
			if (MWClient.isOnLocalServer()) {
				InteractionManager.setTickRate(integratedInteractionRate);
			}
		}
	}

	public static Scrolling scrolling = new Scrolling();

	@AConfigBackground("textures/block/dark_prismarine.png")
	public static class Scrolling {
		public boolean enable = true;
		public boolean invert = false;
		public boolean directionalScrolling = true;
		public boolean scrollCreativeMenuItems = true;
		public boolean scrollCreativeMenuTabs = true;
	}

	public static Sort sort = new Sort();

	@AConfigBackground("textures/block/barrel_top.png")
	public static class Sort {
		public SortMode primarySort = SortMode.RAW_ID;
		public SortMode shiftSort = SortMode.QUANTITY;
		public SortMode controlSort = SortMode.ALPHABET;
	}

	public static Refill refill = new Refill();

	@AConfigBackground("textures/block/horn_coral_block.png")
	public static class Refill {
		public boolean offHand = true;
		public boolean restoreSelectedSlot = false;

		public boolean eat = true;
		public boolean drop = true;

		public boolean use = true;

		public boolean other = true;

		public Rules rules = new Rules();

		@AConfigBackground("textures/block/yellow_terracotta.png")
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

	public static ToolPicking toolPicking = new ToolPicking();

	@AConfigBackground("textures/block/coarse_dirt.png")
	public static class ToolPicking {
		public boolean holdTool = true;
		public boolean holdBlock = false;
		public boolean pickFromInventory = true;
	}

	@AConfigFixer
	public <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void fixConfig(O dataObject, O rootObject) {
		if (dataObject.has("general") && dataObject.get("general").isObject()) {
			O general = dataObject.get("general").asObject();

			moveConfigEntry(dataObject, general, "enable-item-scrolling", "scrolling");
			moveConfigEntry(dataObject, general, "scroll-factor", "scrolling");
			moveConfigEntry(dataObject, general, "directional-scrolling", "scrolling");

			if (dataObject.has("scrolling") && dataObject.get("scrolling").isObject()) {
				O scrolling = dataObject.get("scrolling").asObject();

				if (scrolling.has("scroll-creative-menu") && scrolling.get("scroll-creative-menu").isBoolean()) {
					scrolling.set("scroll-creative-menu-items", !scrolling.get("scroll-creative-menu").asBoolean());
					scrolling.remove("scroll-creative-menu");
				}
				if (scrolling.has("scroll-factor") && scrolling.get("scroll-factor").isNumber()) {
					scrolling.set("invert", scrolling.get("scroll-factor").asFloat() < 0);
					scrolling.remove("scroll-factor");
				}
			}

			moveConfigEntry(dataObject, general, "hold-tool-pick", "tool-picking", "hold-tool");
			moveConfigEntry(dataObject, general, "hold-block-tool-pick", "tool-picking", "hold-block");

			moveConfigEntry(dataObject, general, "enable-alt-dropping", "general", "enable-drop-modifier");

			general.remove("hotbar-scope");
		}
	}

	@SuppressWarnings("SameParameterValue")
	private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void moveConfigEntry(O root, O origin, String name, String destCat) {
		moveConfigEntry(root, origin, name, destCat, name);
	}

	private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void moveConfigEntry(O root, O origin, String name, String destCat, String newName) {
		if (origin.has(name)) {
			O dest;
			if (root.has(destCat) && root.get(destCat).isObject()) {
				dest = root.get(destCat).asObject();
			} else {
				dest = root.addObject(destCat);
			}
			dest.set(newName, origin.get(name));
			origin.remove(name);
		}
	}
}
