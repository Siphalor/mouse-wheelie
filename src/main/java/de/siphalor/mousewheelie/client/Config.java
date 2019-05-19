package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.Core;
import de.siphalor.mousewheelie.client.util.SortMode;
import de.siphalor.tweed.config.ConfigCategory;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigFile;
import de.siphalor.tweed.config.TweedRegistry;
import de.siphalor.tweed.config.entry.EnumEntry;
import de.siphalor.tweed.config.entry.FloatEntry;

@SuppressWarnings("unchecked")
public class Config {
	public static ConfigFile configFile = TweedRegistry.registerConfigFile(Core.MOD_ID);

	public static ConfigCategory sortCategory = configFile.register("sort", new ConfigCategory())
		.setComment("Change sort modes. Existing sort modes are ALPHABET, RAW_ID and QUANTITY");
	public static EnumEntry<SortMode> primarySort = (EnumEntry<SortMode>) sortCategory.register("primary-sort", new EnumEntry<>(SortMode.RAW_ID))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Sets the sort mode for sorting via middle mouse click.")
		;
	public static EnumEntry<SortMode> shiftSort = (EnumEntry<SortMode>) sortCategory.register("shift-sort", new EnumEntry<>(SortMode.QUANTITY))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Sets the sort mode for sorting via shift + middle mouse click.");
	public static EnumEntry<SortMode> controlSort = (EnumEntry<SortMode>) sortCategory.register("control-sort", new EnumEntry<>(SortMode.ALPHABET))
		.setEnvironment(ConfigEnvironment.CLIENT)
		.setComment("Sets the sort mode for sorting via control + middle mouse click.");
		;

	public static ConfigCategory generalCategory = configFile.register("general", new ConfigCategory());
	public static FloatEntry scrollFactor = generalCategory.register("scroll-factor", new FloatEntry(-1.0F))
		.setComment("Set the scroll factor for item scrolling." + System.lineSeparator() +
			"To invert the scrolling use negative numbers");

	public static void initialize() {

	}
}
