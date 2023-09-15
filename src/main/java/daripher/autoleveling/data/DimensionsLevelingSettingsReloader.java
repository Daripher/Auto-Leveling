package daripher.autoleveling.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import daripher.autoleveling.config.Config;
import java.util.Map;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.loot.LootSerializers;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DimensionsLevelingSettingsReloader extends JsonReloadListener {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final Gson GSON = LootSerializers.createLootTableSerializer().create();
  private static Map<ResourceLocation, LevelingSettings> settings = ImmutableMap.of();

  public DimensionsLevelingSettingsReloader() {
    super(GSON, "leveling_settings/dimensions");
  }

  public static LevelingSettings getSettingsForDimension(RegistryKey<World> dimension) {
    return settings.getOrDefault(dimension.location(), defaultSettings());
  }

  private static LevelingSettings defaultSettings() {
    int startingLevel = Config.COMMON.defaultStartingLevel.get();
    int maxLevel = Config.COMMON.defaultMaxLevel.get();
    float levelPerDistance = Config.COMMON.defaultLevelsPerDistance.get().floatValue();
    float levelPerDeepness = Config.COMMON.defaultLevelsPerDeepness.get().floatValue();
    int randomLevelBonus = Config.COMMON.defaultRandomLevelBonus.get();
    return new LevelingSettings(
        startingLevel, maxLevel, levelPerDistance, levelPerDeepness, randomLevelBonus);
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> map,
      IResourceManager resourceManager,
      IProfiler profiler) {
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

    ImmutableMap<ResourceLocation, LevelingSettings> immutableMap = builder.build();
    settings = immutableMap;
  }
}
