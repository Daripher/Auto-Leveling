package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
import daripher.autoleveling.config.Config;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public class DimensionLevelingSettings extends LevelingSettings {
  private final @Nullable BlockPos spawnPosOverride;
  private final float levelsPerDay;

  public DimensionLevelingSettings(
      int startingLevel,
      int maxLevel,
      float levelsPerDistance,
      float levelsPerDeepness,
      int randomLevelBonus,
      @Nullable BlockPos spawnPosOverride,
      float levelsPerDay,
      float levelPowerPerDistance,
      float levelPowerPerDeepness) {
    super(
        startingLevel,
        maxLevel,
        levelsPerDistance,
        levelsPerDeepness,
        randomLevelBonus,
        levelPowerPerDistance,
        levelPowerPerDeepness);
    this.spawnPosOverride = spawnPosOverride;
    this.levelsPerDay = levelsPerDay;
  }

  public Optional<BlockPos> getSpawnPosOverride() {
    return Optional.ofNullable(spawnPosOverride);
  }

  public float getLevelsPerDay() {
    return levelsPerDay;
  }

  public static DimensionLevelingSettings load(JsonObject jsonObject) {
    return new DimensionLevelingSettings(
        jsonObject.get("starting_level").getAsInt(),
        jsonObject.get("max_level").getAsInt(),
        jsonObject.get("levels_per_distance").getAsFloat(),
        jsonObject.get("levels_per_deepness").getAsFloat(),
        jsonObject.get("random_level_bonus").getAsInt(),
        readOptionalBlockPos(jsonObject, "spawn_pos_override").orElse(null),
        readOptionalFloat(jsonObject, "levels_per_day")
            .orElse(Config.COMMON.defaultLevelsPerDay.get().floatValue()),
        readOptionalFloat(jsonObject, "level_power_per_distance")
            .orElse(Config.COMMON.defaultLevelPowerPerDistance.get().floatValue()),
        readOptionalFloat(jsonObject, "level_power_per_deepness")
            .orElse(Config.COMMON.defaultLevelPowerPerDeepness.get().floatValue()));
  }
}
