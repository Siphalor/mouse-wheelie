package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.Core;
import de.siphalor.mousewheelie.client.util.ToolPickMode;
import de.siphalor.mousewheelie.client.util.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.util.inventory.SortMode;
import de.siphalor.tweed.config.ConfigCategory;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigFile;
import de.siphalor.tweed.config.TweedRegistry;
import de.siphalor.tweed.config.entry.BooleanEntry;
import de.siphalor.tweed.config.entry.EnumEntry;
import de.siphalor.tweed.config.entry.FloatEntry;

@SuppressWarnings("unchecked")
public class Config {
	public static ConfigFile configFile = TweedRegistry.registerConfigFile(Core.MOD_ID);

	public static ConfigCategory sortCategory = configFile.register("sort", new ConfigCategory())
		.setComment("Change sort modes. Existing sort modes are ALPHABET, RAW_ID and QUANTITY");
	public static EnumEntry<SortMode.Predefined> primarySort = (EnumEntry<SortMode.Predefined>) sortCategory.register("primary-sort", new EnumEntry<>(SortMode.Predefined.RAW_ID))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Sets the sort mode for sorting via middle mouse click.");
	public static EnumEntry<SortMode.Predefined> shiftSort = (EnumEntry<SortMode.Predefined>) sortCategory.register("shift-sort", new EnumEntry<>(SortMode.Predefined.QUANTITY))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Sets the sort mode for sorting via shift + middle mouse click.");
	public static EnumEntry<SortMode.Predefined> controlSort = (EnumEntry<SortMode.Predefined>) sortCategory.register("control-sort", new EnumEntry<>(SortMode.Predefined.ALPHABET))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Sets the sort mode for sorting via control + middle mouse click.");

	public static ConfigCategory generalCategory = configFile.register("general", new ConfigCategory())
		.setComment("General settings");
	public static FloatEntry scrollFactor = generalCategory.register("scroll-factor", new FloatEntry(-1.0F))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Set the scroll factor for item scrolling." + System.lineSeparator() +
			"To invert the scrolling use negative numbers");
	public static EnumEntry<ToolPickMode> toolPickMode = (EnumEntry<ToolPickMode>) generalCategory.register("tool-pick-mode", new EnumEntry<>(ToolPickMode.HOLD_TOOL))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Sets when to pick the optimal tool to mine the targetted block.");

	public static ConfigCategory refillCategory = configFile.register("refill", new ConfigCategory())
		.setComment("Configure refill related stuff here.");
	public static BooleanEntry eatRefill = refillCategory.register("eat", new BooleanEntry(true))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Refill when eating items");
	public static BooleanEntry otherRefill = refillCategory.register("other", new BooleanEntry(true))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Refill on other occasions");
	public static ConfigCategory refillRules = refillCategory.register("rules", new ConfigCategory())
		.setComment("Enable/Disable specific rules for how to refill items");

	public static void initialize() {
		SlotRefiller.initialize();
	}
}
