package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
import daripher.autoleveling.config.Config;

public class EntityLevelingSettings extends LevelingSettings {
  public EntityLevelingSettings(
      int startingLevel,
      int maxLevel,
      float levelsPerDistance,
      float levelsPerDeepness,
      int randomLevelBonus,
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
}
