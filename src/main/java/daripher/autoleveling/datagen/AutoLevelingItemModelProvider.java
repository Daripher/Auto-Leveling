package daripher.autoleveling.datagen;

import java.util.Objects;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoLevelingItemModelProvider extends ItemModelProvider {
	public AutoLevelingItemModelProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
		super(gen, AutoLevelingMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		registerHandheldModel(AutoLevelingItems.BLACKLIST_TOOL.get());
		registerHandheldModel(AutoLevelingItems.WHITELIST_TOOL.get());
	}

	private ItemModelBuilder registerHandheldModel(Item item) {
		ResourceLocation itemId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
		return withExistingParent(itemId.toString(), mcLoc("handheld")).texture("layer0", modLoc("item/" + itemId.getPath()));
	}
}
