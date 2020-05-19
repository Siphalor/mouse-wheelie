package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.inventory.sort.SortMode;
import de.siphalor.tweed.config.ConfigCategory;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigFile;
import de.siphalor.tweed.config.TweedRegistry;
import de.siphalor.tweed.config.entry.BooleanEntry;
import de.siphalor.tweed.config.entry.EnumEntry;
import de.siphalor.tweed.config.entry.FloatEntry;
import de.siphalor.tweed.config.fixers.ConfigEntryFixer;
import de.siphalor.tweed.config.fixers.ConfigEntryLocationFixer;
import de.siphalor.tweed.data.DataObject;
import de.siphalor.tweed.data.DataValue;
import de.siphalor.tweed.data.serializer.HjsonSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@Environment(EnvType.CLIENT)
public class Config {
	public static ConfigFile configFile = TweedRegistry.registerConfigFile(MouseWheelie.MOD_ID, HjsonSerializer.INSTANCE).setEnvironment(ConfigEnvironment.CLIENT);

	public static ConfigCategory generalCategory = configFile.register("general", new ConfigCategory())
			.setComment("General settings");
	public static BooleanEntry holdToolPick = generalCategory.register("hold-tool-pick", new BooleanEntry(true))
			.setComment("Pick correct tool when middle clicking whilst holding a tool.");
	public static BooleanEntry holdBlockToolPick = generalCategory.register("hold-block-tool-pick", new BooleanEntry(false))
			.setComment("Pick correct tool when middle clicking the held block.");
	public static BooleanEntry enableQuickCraft = generalCategory.register("enable-quick-craft", new BooleanEntry(true))
			.setComment("Enables right-clicking in recipe books/villager trading to swiftly craft/trade.");

	public static ConfigCategory scrollingCategory = configFile.register("scrolling", new ConfigCategory());
	public static BooleanEntry enableItemScrolling = scrollingCategory.register("enable", new BooleanEntry(true))
			.setComment("Enables scrolling of items/stacks.");
	public static FloatEntry scrollFactor = scrollingCategory.register("scroll-factor", new FloatEntry(1F))
			.setComment("Set the scroll factor for item scrolling.\n" +
					"To invert the scrolling use negative numbers");
	public static BooleanEntry directionalScrolling = scrollingCategory.register("directional-scrolling", new BooleanEntry(true))
			.setComment("If enabled items will be moved according to whether your scrolling up or down.\n" +
					"If disabled you will scroll to change the amount of items present (up will increase - down will decrease the amount).");
	public static BooleanEntry pushHotbarSeparately = scrollingCategory.register("push-hotbar-separately", new BooleanEntry(false))
			.setComment("If enabled the player inventory and the hotbar will be treated as different sections when pushing the inventory");
	public static BooleanEntry scrollCreativeMenu = scrollingCategory.register("scroll-creative-menu", new BooleanEntry(false))
			.setComment("Sets whether scrolling in creative mode by default scrolls the items or the menu.");

	public static ConfigCategory sortCategory = configFile.register("sort", new ConfigCategory())
			.setComment("Change sort modes. Existing sort modes are ALPHABET, RAW_ID and QUANTITY");
	public static EnumEntry<SortMode.Predefined> primarySort = (EnumEntry<SortMode.Predefined>) sortCategory.register("primary-sort", new EnumEntry<>(SortMode.Predefined.RAW_ID))
			.setComment("Sets the sort mode for sorting via middle mouse click.");
	public static EnumEntry<SortMode.Predefined> shiftSort = (EnumEntry<SortMode.Predefined>) sortCategory.register("shift-sort", new EnumEntry<>(SortMode.Predefined.QUANTITY))
			.setComment("Sets the sort mode for sorting via shift + middle mouse click.");
	public static EnumEntry<SortMode.Predefined> controlSort = (EnumEntry<SortMode.Predefined>) sortCategory.register("control-sort", new EnumEntry<>(SortMode.Predefined.ALPHABET))
			.setComment("Sets the sort mode for sorting via control + middle mouse click.");

	public static ConfigCategory refillCategory = configFile.register("refill", new ConfigCategory())
			.setComment("Configure refill related stuff here.");
	public static BooleanEntry eatRefill = refillCategory.register("eat", new BooleanEntry(true))
			.setComment("Refill when eating items");
	public static BooleanEntry dropRefill = refillCategory.register("drop", new BooleanEntry(true))
			.setComment("Refill when dropping items");
	public static BooleanEntry useRefill = refillCategory.register("use", new BooleanEntry(true))
			.setComment("Refill when using items");
	public static BooleanEntry otherRefill = refillCategory.register("other", new BooleanEntry(true))
			.setComment("Refill on other occasions");
	public static ConfigCategory refillRules = refillCategory.register("rules", new ConfigCategory())
			.setComment("Enable/Disable specific rules for how to refill items");

	public static void initialize() {
		SlotRefiller.initialize();

		configFile.register("general.tool-pick-mode", new ConfigEntryFixer() {
			@Override
			public void fix(DataObject dataObject, String propertyName, DataObject mainCompound) {
				if (dataObject.has(propertyName)) {
					DataValue dataValue = dataObject.get(propertyName);
					if (!dataValue.isString()) return;
					if (dataValue.asString().equals("HOLD_TOOL")) {
						mainCompound.set("hold-tool-pick", true);
					} else if (dataValue.asString().equals("SHIFT")) {
						mainCompound.set("hold-tool-pick", false);
					}
					dataObject.remove(propertyName);
				}
			}
		});

		configFile.register("general.enable-item-scrolling", new ConfigEntryLocationFixer("enable", "scrolling"));
		configFile.register("general.scroll-factor", new ConfigEntryLocationFixer("scroll-factor", "scrolling"));
		configFile.register("general.directional-scrolling", new ConfigEntryLocationFixer("directional-scrolling", "scrolling"));
		configFile.register("general.push-hotbar-separately", new ConfigEntryLocationFixer("push-hotbar-separately", "scrolling"));
	}
}
