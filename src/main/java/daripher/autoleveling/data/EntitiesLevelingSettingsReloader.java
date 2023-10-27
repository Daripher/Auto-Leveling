package daripher.autoleveling.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import daripher.autoleveling.settings.EntityLevelingSettings;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public class EntitiesLevelingSettingsReloader extends SimpleJsonResourceReloadListener {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Gson GSON = Deserializers.createLootTableSerializer().create();
  private static final Map<ResourceLocation, EntityLevelingSettings> SETTINGS = new HashMap<>();

  public EntitiesLevelingSettingsReloader() {
    super(GSON, "leveling_settings/entities");
  }

  @Nullable
  public static EntityLevelingSettings getSettingsForEntity(EntityType<?> entityType) {
    return SETTINGS.get(ForgeRegistries.ENTITIES.getKey(entityType));
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> map,
      ResourceManager resourceManager,
      ProfilerFiller profilerFiller) {
    SETTINGS.clear();
    map.forEach(this::loadSettings);
  }

  private void loadSettings(ResourceLocation fileId, JsonElement jsonElement) {
    try {
      LOGGER.info("Loading leveling settings {}", fileId);
      EntityLevelingSettings settings = EntityLevelingSettings.load(jsonElement.getAsJsonObject());
      SETTINGS.put(fileId, settings);
    } catch (Exception exception) {
      LOGGER.error("Couldn't parse leveling settings {}", fileId, exception);
    }
  }
}
