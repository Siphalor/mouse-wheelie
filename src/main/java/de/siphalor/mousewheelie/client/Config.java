package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigScope;
import de.siphalor.tweed.config.annotated.AConfigEntry;
import de.siphalor.tweed.config.annotated.ATweedConfig;

@SuppressWarnings({"WeakerAccess", "unused"})
@ATweedConfig(environment = ConfigEnvironment.CLIENT, scope = ConfigScope.SMALLEST, tailors = "tweed:cloth")
public class Config {
	@AConfigEntry(comment = "General settings")
	public General general = new General();

	public static class General {
		@AConfigEntry(name = "enable-item-scrolling", comment = "Enables scrolling of stacks")
		public boolean enableItemScrolling = true;

		@AConfigEntry(name = "directional-scrolling", comment = "If enabled items will be moved according to whether your scrolling up or down.\nIf disabled you will scroll to change the amount of items present (up will increase - down will decrease")
		public boolean directionalScrolling = true;

		@AConfigEntry(name = "scroll-factor", comment = "Set the scroll factor for item scrolling.\nTo invert the scrolling use negative numbers")
		public float scrollFactor = -1F;

		@AConfigEntry(name = "hold-tool-pick", comment = "Pick correct tool when middle clicking whilst holding a tool.")
		public boolean holdToolPick = true;

		@AConfigEntry(name = "hold-block-tool-pick", comment = "Pick correct tool when middle clicking the held block.")
		public boolean holdBlockToolPick = false;

		@AConfigEntry(name = "enable-quick-craft", comment = "Enables right-clicking in recipe books/villager trading to swiftly craft/trade.")
		public boolean enableQuickCraft = true;
	}

	@AConfigEntry(comment = "Change sort modes. Existing sort modes are ALPHABET, RAW_ID and QUANTITY")
	public Sort sort = new Sort();

	public static class Sort {
		@AConfigEntry(name = "primary-sort", comment = "Sets the sort mode for normal sorting.")
		public SortMode.Predefined primarySort = SortMode.Predefined.RAW_ID;

		@AConfigEntry(name = "shift-sort", comment = "Sets the sort mode for sorting whilst pressing shift.")
		public SortMode.Predefined shiftSort = SortMode.Predefined.QUANTITY;

		@AConfigEntry(name = "control-sort", comment = "Sets the sort mode for sorting whilst pressing control.")
		public SortMode.Predefined controlSort = SortMode.Predefined.ALPHABET;
	}

	@AConfigEntry(comment = "Configure refill related stuff here.")
	public Refill refill = new Refill();

	public static class Refill {
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

		public static class Rules {
			@AConfigEntry(name = "any-block", comment = "Tries to find any block items")
			public boolean anyBlock = false;
			@AConfigEntry(name = "itemgroup", comment = "Find items of the same item group")
			public boolean itemgroup = false;
			@AConfigEntry(name = "item-hierarchy", comment = "Try to find similar items through the item type hierarchy")
			public boolean itemHierarchy = false;
			@AConfigEntry(name = "block-hierarchy", comment = "Try to find similar block items through the block type hierarchy")
			public boolean blockHierarchy = false;
			@AConfigEntry(comment = "Try to find other food items")
			public boolean food = false;
			@AConfigEntry(name = "equal-items", comment = "Try to find equal items (no nbt matching)")
			public boolean equalItems = true;
			@AConfigEntry(name = "equal-stacks", comment = "Try to find equal stacks (nbt matching")
			public boolean equalStacks = true;
		}
	}
}
