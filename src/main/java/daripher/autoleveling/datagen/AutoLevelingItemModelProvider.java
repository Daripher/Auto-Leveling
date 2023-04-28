package daripher.autoleveling.datagen;

import java.util.Objects;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoLevelingItemModelProvider extends ItemModelProvider {
	public AutoLevelingItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
		super(packOutput, AutoLevelingMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		handheld(AutoLevelingItems.BLACKLIST_TOOL.get());
		handheld(AutoLevelingItems.WHITELIST_TOOL.get());
	}

	private ItemModelBuilder handheld(Item item) {
		ResourceLocation itemId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
		return withExistingParent(itemId.toString(), mcLoc("handheld")).texture("layer0", modLoc("item/" + itemId.getPath()));
	}
}
