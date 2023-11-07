package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
import daripher.autoleveling.config.Config;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public record DimensionLevelingSettings(
    int startingLevel,
    int maxLevel,
    float levelsPerDistance,
    float levelsPerDeepness,
    int randomLevelBonus,
    Optional<BlockPos> spawnPosOverride,
    float levelsPerDay,
    float levelPowerPerDistance,
    float levelPowerPerDeepness)
    implements LevelingSettings {
  public static DimensionLevelingSettings load(JsonObject jsonObject) {
    return new DimensionLevelingSettings(
        jsonObject.get("starting_level").getAsInt(),
        jsonObject.get("max_level").getAsInt(),
        jsonObject.get("levels_per_distance").getAsFloat(),
        jsonObject.get("levels_per_deepness").getAsFloat(),
        jsonObject.get("random_level_bonus").getAsInt(),
        loadOptionalBlockPos(jsonObject, "spawn_pos_override"),
        readOptionalFloat(jsonObject, "levels_per_day")
            .orElse(Config.COMMON.defaultLevelsPerDay.get().floatValue()),
        readOptionalFloat(jsonObject, "level_power_per_distance")
            .orElse(Config.COMMON.defaultLevelPowerPerDistance.get().floatValue()),
        readOptionalFloat(jsonObject, "level_power_per_deepness")
            .orElse(Config.COMMON.defaultLevelPowerPerDeepness.get().floatValue()));
  }

  private static Optional<Float> readOptionalFloat(JsonObject jsonObject, String name) {
    if (!jsonObject.has(name)) {
      return Optional.empty();
    } else {
      return Optional.of(jsonObject.get(name).getAsFloat());
    }
  }

  @NotNull
  private static Optional<BlockPos> loadOptionalBlockPos(JsonObject jsonObject, String name) {
    if (!jsonObject.has(name)) {
      return Optional.empty();
    } else {
      JsonObject posJson = jsonObject.get(name).getAsJsonObject();
      int x = posJson.get("x").getAsInt();
      int y = posJson.get("y").getAsInt();
      int z = posJson.get("z").getAsInt();
      return Optional.of(new BlockPos(x, y, z));
    }
  }
}
