package daripher.autoleveling.datagen;

import static net.minecraft.util.text.TextFormatting.YELLOW;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

public class AutoLevelingLanguageProvider extends LanguageProvider {
	public AutoLevelingLanguageProvider(DataGenerator gen) {
		super(gen, AutoLevelingMod.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add("itemGroup.autoleveling", "Auto Leveling Tools");
		add(AutoLevelingItems.BLACKLIST_TOOL.get(), "Blacklist Tool");
		addSpecialText(AutoLevelingItems.BLACKLIST_TOOL.get(), "tooltip", YELLOW + "Adds or removes entity from blacklist");
		addSpecialText(AutoLevelingItems.BLACKLIST_TOOL.get(), "removed", "%s was removed from blacklist");
		addSpecialText(AutoLevelingItems.BLACKLIST_TOOL.get(), "added", "%s was added to blacklist");
		add(AutoLevelingItems.WHITELIST_TOOL.get(), "Whitelist Tool");
		addSpecialText(AutoLevelingItems.WHITELIST_TOOL.get(), "tooltip", YELLOW + "Adds or removes entity from whitelist");
		addSpecialText(AutoLevelingItems.WHITELIST_TOOL.get(), "removed", "%s was removed from whitelist");
		addSpecialText(AutoLevelingItems.WHITELIST_TOOL.get(), "added", "%s was added to whitelist");
		add("jade.autoleveling.tooltip", "Level: %d");
		add("config.jade.plugin_autoleveling.level", "Level");
	}

	public void addSpecialText(Item key, String type, String translation) {
		add(key.getDescriptionId() + "." + type, translation);
	}
}
