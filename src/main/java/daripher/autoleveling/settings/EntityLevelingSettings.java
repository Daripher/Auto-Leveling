package daripher.autoleveling.settings;

import com.google.gson.JsonObject;

public record EntityLevelingSettings(
    int startingLevel,
    int maxLevel,
    float levelsPerDistance,
    float levelsPerDeepness,
    int randomLevelBonus)
    implements LevelingSettings {
  public static EntityLevelingSettings load(JsonObject jsonObject) {
    int startingLevel = jsonObject.get("starting_level").getAsInt();
    int maxLevel = jsonObject.get("max_level").getAsInt();
    float levelsPerDistance = jsonObject.get("levels_per_distance").getAsFloat();
    float levelsPerDeepness = jsonObject.get("levels_per_deepness").getAsFloat();
    int randomLevelBonus = jsonObject.get("random_level_bonus").getAsInt();
    return new EntityLevelingSettings(
        startingLevel, maxLevel, levelsPerDistance, levelsPerDeepness, randomLevelBonus);
  }
}
