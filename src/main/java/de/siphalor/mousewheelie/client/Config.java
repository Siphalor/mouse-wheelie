package de.siphalor.mousewheelie.client;

import de.siphalor.mousewheelie.MouseWheelie;
import de.siphalor.mousewheelie.client.util.inventory.SlotRefiller;
import de.siphalor.mousewheelie.client.util.inventory.SortMode;
import de.siphalor.tweed.config.ConfigCategory;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigFile;
import de.siphalor.tweed.config.TweedRegistry;
import de.siphalor.tweed.config.entry.BooleanEntry;
import de.siphalor.tweed.config.entry.EnumEntry;
import de.siphalor.tweed.config.entry.FloatEntry;
import de.siphalor.tweed.config.fixers.ConfigEntryFixer;
import de.siphalor.tweed.data.DataObject;
import de.siphalor.tweed.data.DataValue;
import de.siphalor.tweed.data.serializer.HjsonSerializer;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class Config {
	public static ConfigFile configFile = TweedRegistry.registerConfigFile(MouseWheelie.MOD_ID, HjsonSerializer.INSTANCE).setEnvironment(ConfigEnvironment.CLIENT);

	public static ConfigCategory generalCategory = configFile.register("general", new ConfigCategory())
		.setComment("General settings");
	public static BooleanEntry enableItemScrolling = generalCategory.register("enable-item-scrolling", new BooleanEntry(true))
		.setComment("Enables scrolling of items/stacks.\n" +
			"(WHY WOULD YOU DARE TO TURN THIS OFF?)");
	public static FloatEntry scrollFactor = generalCategory.register("scroll-factor", new FloatEntry(-1.0F))
		.setComment("Set the scroll factor for item scrolling.\n" +
			"To invert the scrolling use negative numbers");
	public static BooleanEntry holdToolPick = generalCategory.register("hold-tool-pick", new BooleanEntry(true))
		.setComment("Pick correct tool when middle clicking whilst holding a tool.\n" +
			"(shift-middle-clicking has been moved to the 'Controls' screen)");
	public static BooleanEntry enableQuickCraft = generalCategory.register("enable-quick-craft", new BooleanEntry(true))
		.setComment("Enables right-clicking in recipe books/villager trading to swiftly craft/trade.");

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
                if(dataObject.has(propertyName)) {
					DataValue dataValue = dataObject.get(propertyName);
					if(!dataValue.isString()) return;
					if(dataValue.asString().equals("HOLD_TOOL")) {
						mainCompound.set("hold-tool-pick", true);
					} else if(dataValue.asString().equals("SHIFT")) {
						mainCompound.set("hold-tool-pick", false);
					}
					dataObject.remove(propertyName);
				}
			}
		});
	}
}
