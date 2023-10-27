package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public record DimensionLevelingSettings(
    int startingLevel,
    int maxLevel,
    float levelsPerDistance,
    float levelsPerDeepness,
    int randomLevelBonus,
    Optional<BlockPos> spawnPosOverride)
    implements LevelingSettings {
  public static DimensionLevelingSettings load(JsonObject jsonObject) {
    int startingLevel = jsonObject.get("starting_level").getAsInt();
    int maxLevel = jsonObject.get("max_level").getAsInt();
    float levelsPerDistance = jsonObject.get("levels_per_distance").getAsFloat();
    float levelsPerDeepness = jsonObject.get("levels_per_deepness").getAsFloat();
    int randomLevelBonus = jsonObject.get("random_level_bonus").getAsInt();
    Optional<BlockPos> spawnPosOverride;
    if (!jsonObject.has("spawn_pos_override")) {
      spawnPosOverride = Optional.empty();
    } else {
      JsonObject posJson = jsonObject.get("spawn_pos_override").getAsJsonObject();
      int x = posJson.get("x").getAsInt();
      int y = posJson.get("y").getAsInt();
      int z = posJson.get("z").getAsInt();
      spawnPosOverride = Optional.of(new BlockPos(x, y, z));
    }
    return new DimensionLevelingSettings(
        startingLevel,
        maxLevel,
        levelsPerDistance,
        levelsPerDeepness,
        randomLevelBonus,
        spawnPosOverride);
  }
}
