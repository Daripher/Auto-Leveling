package daripher.autoleveling.settings;

import com.google.gson.JsonObject;

public record EntityLevelingSettings(
    int startingLevel,
    int maxLevel,
    float levelsPerDistance,
    float levelsPerDeepness,
    int randomLevelBonus)
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
        jsonObject.get("random_level_bonus").getAsInt());
  }
}
