package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
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
      float levelsPerDay) {
    super(startingLevel, maxLevel, levelsPerDistance, levelsPerDeepness, randomLevelBonus);
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
        jsonObject.get("levels_per_day").getAsFloat());
  }

  private static Optional<BlockPos> readOptionalBlockPos(JsonObject jsonObject, String name) {
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
