package daripher.autoleveling.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.settings.DimensionLevelingSettings;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class DimensionsLevelingSettingsReloader extends SimpleJsonResourceReloadListener {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Gson GSON = Deserializers.createLootTableSerializer().create();
  private static final Map<ResourceLocation, DimensionLevelingSettings> SETTINGS = new HashMap<>();

  public DimensionsLevelingSettingsReloader() {
    super(GSON, "leveling_settings/dimensions");
  }

  @Nonnull
  public static DimensionLevelingSettings get(ResourceKey<Level> dimension) {
    return SETTINGS.getOrDefault(dimension.location(), createDefaultSettings());
  }

  private static DimensionLevelingSettings createDefaultSettings() {
    return new DimensionLevelingSettings(
        Config.COMMON.startingLevel.get(),
        Config.COMMON.maxLevel.get(),
        Config.COMMON.levelsPerDistance.get().floatValue(),
        Config.COMMON.levelsPerDeepness.get().floatValue(),
        Config.COMMON.randomLevelBonus.get(),
        null,
        Config.COMMON.levelsPerDay.get().floatValue(),
        Config.COMMON.levelPowerPerDistance.get().floatValue(),
        Config.COMMON.levelPowerPerDeepness.get().floatValue(),
        null);
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> jsonElements,
      @NotNull ResourceManager resourceManager,
      @NotNull ProfilerFiller profilerFiller) {
    SETTINGS.clear();
    jsonElements.forEach(this::loadSettings);
  }

  private void loadSettings(ResourceLocation fileId, JsonElement jsonElement) {
    try {
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      DimensionLevelingSettings settings = DimensionLevelingSettings.load(jsonObject);
      SETTINGS.put(fileId, settings);
      LOGGER.info("Loaded leveling settings {}", fileId);
    } catch (Exception exception) {
      LOGGER.error("Couldn't load leveling settings {}", fileId);
      exception.printStackTrace();
    }
  }
}
