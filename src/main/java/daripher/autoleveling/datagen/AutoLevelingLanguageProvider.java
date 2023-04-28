package daripher.autoleveling.datagen;

import static net.minecraft.ChatFormatting.*;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

public class AutoLevelingLanguageProvider extends LanguageProvider {
	public AutoLevelingLanguageProvider(DataGenerator gen) {
		super(gen, AutoLevelingMod.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add("itemGroup.autoleveling", "Auto Leveling Tools");
		add(AutoLevelingItems.BLACKLIST_TOOL.get(), "Blacklist Tool");
		specialText(AutoLevelingItems.BLACKLIST_TOOL.get(), "tooltip", YELLOW + "Adds or removes entity from blacklist");
		specialText(AutoLevelingItems.BLACKLIST_TOOL.get(), "removed", "%s was removed from blacklist");
		specialText(AutoLevelingItems.BLACKLIST_TOOL.get(), "added", "%s was added to blacklist");
		add(AutoLevelingItems.WHITELIST_TOOL.get(), "Whitelist Tool");
		specialText(AutoLevelingItems.WHITELIST_TOOL.get(), "tooltip", YELLOW + "Adds or removes entity from whitelist");
		specialText(AutoLevelingItems.WHITELIST_TOOL.get(), "removed", "%s was removed from whitelist");
		specialText(AutoLevelingItems.WHITELIST_TOOL.get(), "added", "%s was added to whitelist");
		add("jade.autoleveling.tooltip", "Level: %d");
		add("config.jade.plugin_autoleveling.level", "Level");
		add("autoleveling.level", "Lv.%s");
	}

	public void specialText(Item key, String type, String translation) {
		add(key.getDescriptionId() + "." + type, translation);
	}
}
