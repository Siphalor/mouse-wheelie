/*
 * Copyright 2021 Siphalor
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
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.tweed4.annotated.*;
import de.siphalor.tweed4.config.ConfigEnvironment;
import de.siphalor.tweed4.config.ConfigScope;
import de.siphalor.tweed4.config.constraints.RangeConstraint;
import de.siphalor.tweed4.data.DataList;
import de.siphalor.tweed4.data.DataObject;
import de.siphalor.tweed4.data.DataValue;

@SuppressWarnings({"WeakerAccess", "unused"})
@ATweedConfig(environment = ConfigEnvironment.CLIENT, scope = ConfigScope.SMALLEST, tailors = "tweed4:coat", casing = CaseFormat.LOWER_HYPHEN)
@AConfigBackground("textures/block/green_concrete_powder.png")
public class MWConfig {
	@AConfigEntry(comment = "General settings")
	public static General general = new General();

	@AConfigBackground("textures/block/acacia_log.png")
	public static class General {
		@AConfigEntry(
				comment = "Sets the interval in milliseconds in which certain packets are fired.\nLower numbers increase the speed but might be rejected by servers.",
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..1000")
		)
		public int interactionRate = 10;

		@AConfigEntry(environment = ConfigEnvironment.UNIVERSAL, comment = "Enables using armor/elytra items to swap them with the currently equipped item.")
		public boolean enableQuickArmorSwapping = true;

		@AConfigEntry(comment = "Enables dropping items when pressing alt and clicking on them.")
		public boolean enableAltDropping = true;

		@AConfigEntry(comment = "Enables right-clicking in recipe books/villager trading to swiftly craft/trade.")
		public boolean enableQuickCraft = true;

		@AConfigEntry(comment = "This option will treat the hotbar as a separate scope.\nThis means that pushing the inventory or sorting the main inventory will not affect the hotbar and vice-versa.")
		public boolean hotbarScope = true;

		@AConfigListener()
		public void onReload() {
			InteractionManager.setTickRate(interactionRate);
		}
	}

	public static Scrolling scrolling = new Scrolling();

	@AConfigBackground("textures/block/dark_prismarine.png")
	public static class Scrolling {
		@AConfigEntry(comment = "Enables scrolling of stacks")
		public boolean enable = true;

		@AConfigEntry(comment = "Invert the scroll direction when scrolling items")
		public boolean invert = false;

		@AConfigEntry(comment = "If enabled items will be moved according to whether your scrolling up or down.\nIf disabled you will scroll to change the amount of items present (up will increase - down will decrease")
		public boolean directionalScrolling = true;

		@AConfigEntry(comment = "Sets whether to by default scroll items\nout of the creative menu.")
		public boolean scrollCreativeMenuItems = true;

		@AConfigEntry(comment = "Sets whether creative mode tabs can\nbe switched by scrolling over them.")
		public boolean scrollCreativeMenuTabs = true;
	}

	@AConfigEntry(comment = "Change sort modes. Existing sort modes are ALPHABET, RAW_ID and QUANTITY")
	public static Sort sort = new Sort();

	@AConfigBackground("textures/block/barrel_top.png")
	public static class Sort {
		@AConfigEntry(comment = "Sets the sort mode for normal sorting.")
		public SortMode primarySort = SortMode.RAW_ID;

		@AConfigEntry(comment = "Sets the sort mode for sorting whilst pressing shift.")
		public SortMode shiftSort = SortMode.QUANTITY;

		@AConfigEntry(comment = "Sets the sort mode for sorting whilst pressing control.")
		public SortMode controlSort = SortMode.ALPHABET;
	}

	@AConfigEntry(comment = "Configure refill related stuff here.")
	public static Refill refill = new Refill();

	@AConfigBackground("textures/block/horn_coral_block.png")
	public static class Refill {
		@AConfigEntry(comment = "Refills stacks in the off hand")
		public boolean offHand = true;

		@AConfigEntry(comment = "Refill when eating items")
		public boolean eat = true;

		@AConfigEntry(comment = "Refill when dropping items")
		public boolean drop = true;

		@AConfigEntry(comment = "Refill when using up items")
		public boolean use = true;

		@AConfigEntry(comment = "Refill on other occasions")
		public boolean other = true;

		@AConfigEntry(comment = "Enable/Disable specific rules for how to refill items")
		public Rules rules = new Rules();

		@AConfigBackground("textures/block/yellow_terracotta.png")
		public static class Rules {
			@AConfigEntry(comment = "Tries to find any block items")
			public boolean anyBlock = false;
			@AConfigEntry(comment = "Find items of the same item group")
			public boolean itemgroup = false;
			@AConfigEntry(comment = "Try to find similar items through the item type hierarchy")
			public boolean itemHierarchy = false;
			@AConfigEntry(comment = "Try to find similar block items through the block type hierarchy")
			public boolean blockHierarchy = false;
			@AConfigEntry(comment = "Try to find other food items")
			public boolean food = false;
			@AConfigEntry(comment = "Try to find equal items (no nbt matching)")
			public boolean equalItems = true;
			@AConfigEntry(comment = "Try to find equal stacks (nbt matching")
			public boolean equalStacks = true;
		}
	}

	@AConfigEntry(comment = "Configure picking the correct tool for the currently faced block.")
	public static ToolPicking toolPicking = new ToolPicking();

	@AConfigBackground("textures/block/coarse_dirt.png")
	public static class ToolPicking {
		@AConfigEntry(comment = "Pick correct tool when middle clicking whilst holding a tool.")
		public boolean holdTool = true;

		@AConfigEntry(comment = "Pick correct tool when middle clicking whilst holding the same block as faced.")
		public boolean holdBlock = false;

		@AConfigEntry(comment = "Allows picking tools from the inventory.\nIf disabled picking will only happen from the hotbar.")
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
