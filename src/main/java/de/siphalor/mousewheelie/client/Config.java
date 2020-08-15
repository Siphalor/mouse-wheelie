package de.siphalor.mousewheelie.client;

import com.google.common.base.CaseFormat;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigScope;
import de.siphalor.tweed.config.annotated.AConfigEntry;
import de.siphalor.tweed.config.annotated.AConfigFixer;
import de.siphalor.tweed.config.annotated.ATweedConfig;
import de.siphalor.tweed.data.DataObject;
import de.siphalor.tweed.data.DataValue;
import net.minecraft.util.Pair;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings({"WeakerAccess", "unused"})
@ATweedConfig(environment = ConfigEnvironment.CLIENT, scope = ConfigScope.SMALLEST, tailors = "tweed:cloth", casing = CaseFormat.LOWER_HYPHEN)
public class Config {
	@AConfigEntry(comment = "General settings")
	public General general = new General();

	public static class General {
		@AConfigEntry(comment = "Pick correct tool when middle clicking whilst holding a tool.")
		public boolean holdToolPick = true;

		@AConfigEntry(comment = "Pick correct tool when middle clicking the held block.")
		public boolean holdBlockToolPick = false;

		@AConfigEntry(comment = "Enables right-clicking in recipe books/villager trading to swiftly craft/trade.")
		public boolean enableQuickCraft = true;
	}

	public Scrolling scrolling = new Scrolling();

	public static class Scrolling {
		@AConfigEntry(comment = "Enables scrolling of stacks")
		public boolean enable = true;

		@AConfigEntry(comment = "Set the scroll factor for item scrolling.\nTo invert the scrolling use negative numbers")
		public float scrollFactor = 1F;

		@AConfigEntry(comment = "If enabled items will be moved according to whether your scrolling up or down.\nIf disabled you will scroll to change the amount of items present (up will increase - down will decrease")
		public boolean directionalScrolling = true;

		@AConfigEntry(comment = "If enabled the player inventory and the hotbar will be treated as different sections when pushing the inventory")
		public boolean pushHotbarSeparately = false;

		@AConfigEntry(comment = "Sets whether scrolling in creative mode by default scrolls the items or the menu.")
		public boolean scrollCreativeMenu = false;
	}

	@AConfigEntry(comment = "Change sort modes. Existing sort modes are ALPHABET, RAW_ID and QUANTITY")
	public Sort sort = new Sort();

	public static class Sort {
		@AConfigEntry(comment = "Sets the sort mode for normal sorting.")
		public SortMode primarySort = SortMode.RAW_ID;

		@AConfigEntry(comment = "Sets the sort mode for sorting whilst pressing shift.")
		public SortMode shiftSort = SortMode.QUANTITY;

		@AConfigEntry(comment = "Sets the sort mode for sorting whilst pressing control.")
		public SortMode controlSort = SortMode.ALPHABET;
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

	@AConfigFixer
	public <T> void fixConfig(DataObject<T> dataObject, String property, DataObject<T> rootObject) {
		if (dataObject.has("general") && dataObject.get("general").isObject()) {
			DataObject<T> general = dataObject.get("general").asObject();

			Queue<Pair<String, DataValue<T>>> values = new ConcurrentLinkedQueue<>();

			if (dataObject.has("enable-item-scrolling")) {
				values.add(new Pair<>("enable", dataObject.get("enable-item-scrolling")));
			}
			if (dataObject.has("scroll-factor")) {
				values.add(new Pair<>("scroll-factor", dataObject.get("scroll-factor")));
			}
			if (dataObject.has("directional-scrolling")) {
				values.add(new Pair<>("directional-scrolling", dataObject.get("directional-scrolling")));
			}

			if (!values.isEmpty()) {
				DataObject<T> scrolling;
				if (dataObject.has("scrolling") && dataObject.get("scrolling").isObject()) {
					scrolling = dataObject.get("scrolling").asObject();
				} else {
					scrolling = dataObject.addObject("scrolling");
				}
				values.forEach(pair -> scrolling.set(pair.getLeft(), pair.getRight()));
			}
		}
	}
}
