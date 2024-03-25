package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record EntityLevelingSettings(
    int startingLevel,
    int maxLevel,
    float levelsPerDistance,
    float levelsPerDeepness,
    int randomLevelBonus,
    float levelPowerPerDistance,
    float levelPowerPerDeepness,
    Map<Attribute, AttributeModifier> attributeModifiers)
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
        LevelingSettings.readLevelPowerPerDistance(jsonObject),
        LevelingSettings.readLevelPowerPerDeepness(jsonObject),
        LevelingSettings.readAttributeModifiers(jsonObject));
  }
}
