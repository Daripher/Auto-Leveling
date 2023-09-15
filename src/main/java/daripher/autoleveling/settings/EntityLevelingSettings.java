package daripher.autoleveling.settings;

import com.google.gson.JsonObject;

public class EntityLevelingSettings extends LevelingSettings {
  public EntityLevelingSettings(
      int startingLevel,
      int maxLevel,
      float levelsPerDistance,
      float levelsPerDeepness,
      int randomLevelBonus) {
    super(startingLevel, maxLevel, levelsPerDistance, levelsPerDeepness, randomLevelBonus);
  }

  public static EntityLevelingSettings load(JsonObject jsonObject) {
    return new EntityLevelingSettings(
        jsonObject.get("starting_level").getAsInt(),
        jsonObject.get("max_level").getAsInt(),
        jsonObject.get("levels_per_distance").getAsFloat(),
        jsonObject.get("levels_per_deepness").getAsFloat(),
        jsonObject.get("random_level_bonus").getAsInt());
  }
}
