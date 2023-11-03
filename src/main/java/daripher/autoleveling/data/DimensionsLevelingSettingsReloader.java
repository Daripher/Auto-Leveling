package daripher.autoleveling.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.settings.DimensionLevelingSettings;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
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
  private static final Map<ResourceLocation, DimensionLevelingSettings> SETTINGS = new HashMap<>();

  public DimensionsLevelingSettingsReloader() {
    super(GSON, "leveling_settings/dimensions");
  }

  public static DimensionLevelingSettings getSettingsForDimension(RegistryKey<World> dimension) {
    return SETTINGS.getOrDefault(dimension.location(), defaultSettings());
  }

  private static DimensionLevelingSettings defaultSettings() {
    return new DimensionLevelingSettings(
        Config.COMMON.defaultStartingLevel.get(),
        Config.COMMON.defaultMaxLevel.get(),
        Config.COMMON.defaultLevelsPerDistance.get().floatValue(),
        Config.COMMON.defaultLevelsPerDeepness.get().floatValue(),
        Config.COMMON.defaultRandomLevelBonus.get(),
        null,
        Config.COMMON.defaultLevelsPerDay.get().floatValue(),
        Config.COMMON.defaultLevelPowerPerDistance.get().floatValue(),
        Config.COMMON.defaultLevelPowerPerDeepness.get().floatValue());
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> map,
      @Nonnull IResourceManager resourceManager,
      @Nonnull IProfiler profiler) {
    SETTINGS.clear();

    map.forEach(
        (id, json) -> {
          try {
            DimensionLevelingSettings settings =
                DimensionLevelingSettings.load(json.getAsJsonObject());
            SETTINGS.put(id, settings);
            LOGGER.info("Loading leveling settings {}", id);
          } catch (Exception exception) {
            LOGGER.error("Couldn't parse leveling settings {}", id, exception);
          }
        });
  }
}
