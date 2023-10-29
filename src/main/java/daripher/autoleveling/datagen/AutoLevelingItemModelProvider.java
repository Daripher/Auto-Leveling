package daripher.autoleveling.datagen;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingItems;
import java.util.Objects;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoLevelingItemModelProvider extends ItemModelProvider {
  public AutoLevelingItemModelProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
    super(gen, AutoLevelingMod.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    handheld(AutoLevelingItems.BLACKLIST_TOOL.get());
    handheld(AutoLevelingItems.WHITELIST_TOOL.get());
  }

  private void handheld(Item item) {
    ResourceLocation itemId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
    withExistingParent(itemId.toString(), mcLoc("handheld")).texture("layer0", modLoc("item/" + itemId.getPath()));
  }
}
