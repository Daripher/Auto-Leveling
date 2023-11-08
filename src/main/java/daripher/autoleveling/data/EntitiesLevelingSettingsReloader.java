package daripher.autoleveling.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootSerializers;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntitiesLevelingSettingsReloader extends JsonReloadListener {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final Gson GSON = LootSerializers.createLootTableSerializer().create();
  private static Map<ResourceLocation, LevelingSettings> settings = ImmutableMap.of();

  public EntitiesLevelingSettingsReloader() {
    super(GSON, "leveling_settings/entities");
  }

  @Nullable
  public static LevelingSettings getSettingsForEntity(EntityType<?> entityType) {
    return settings.get(ForgeRegistries.ENTITIES.getKey(entityType));
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> map,
      @Nonnull IResourceManager resourceManager,
      @Nonnull IProfiler profiler) {
    ImmutableMap.Builder<ResourceLocation, LevelingSettings> builder = ImmutableMap.builder();

    map.forEach(
        (id, json) -> {
          try {
            LevelingSettings levelingSettings = LevelingSettings.load(json.getAsJsonObject());
            builder.put(id, levelingSettings);
            LOGGER.info("Loading leveling settings {}", id);
          } catch (Exception exception) {
            LOGGER.error("Couldn't parse leveling settings {}", id, exception);
          }
        });

    settings = builder.build();
  }
}
