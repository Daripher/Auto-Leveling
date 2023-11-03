package daripher.autoleveling.settings;

import com.google.gson.JsonObject;

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
        jsonObject.get("level_power_per_distance").getAsFloat(),
        jsonObject.get("level_power_per_deepness").getAsFloat());
  }
}
