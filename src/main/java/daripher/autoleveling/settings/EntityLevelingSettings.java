package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
import daripher.autoleveling.config.Config;
import java.util.Optional;

public record EntityLevelingSettings(
    int startingLevel,
    int maxLevel,
    float levelsPerDistance,
    float levelsPerDeepness,
    int randomLevelBonus,
    float levelPowerPerDistance,
    float levelPowerPerDeepness)
    implements LevelingSettings {

  @Override
  public float levelsPerDay() {
    return 0f;
  }

  public static EntityLevelingSettings load(JsonObject jsonObject) {
    return new EntityLevelingSettings(
        jsonObject.get("starting_level").getAsInt(),
        jsonObject.get("max_level").getAsInt(),
        jsonObject.get("levels_per_distance").getAsFloat(),
        jsonObject.get("levels_per_deepness").getAsFloat(),
        jsonObject.get("random_level_bonus").getAsInt(),
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
}
