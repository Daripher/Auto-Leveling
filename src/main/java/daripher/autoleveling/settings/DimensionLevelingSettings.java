package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
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
    float levelsPerDay)
    implements LevelingSettings {
  public static DimensionLevelingSettings load(JsonObject jsonObject) {
    return new DimensionLevelingSettings(
        jsonObject.get("starting_level").getAsInt(),
        jsonObject.get("max_level").getAsInt(),
        jsonObject.get("levels_per_distance").getAsFloat(),
        jsonObject.get("levels_per_deepness").getAsFloat(),
        jsonObject.get("random_level_bonus").getAsInt(),
        loadOptionalBlockPos(jsonObject, "spawn_pos_override"),
        jsonObject.get("levels_per_day").getAsFloat());
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
