package daripher.autoleveling.settings;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public abstract class LevelingSettings {
  public final int startingLevel;
  public final int maxLevel;
  public final float levelsPerDistance;
  public final float levelsPerDeepness;
  public final int randomLevelBonus;
  public final float levelPowerPerDistance;
  public final float levelPowerPerDeepness;

  public LevelingSettings(
      int startingLevel,
      int maxLevel,
      float levelsPerDistance,
      float levelsPerDeepness,
      int randomLevelBonus,
      float levelPowerPerDistance,
      float levelPowerPerDeepness) {
    this.startingLevel = startingLevel;
    this.maxLevel = maxLevel;
    this.levelsPerDistance = levelsPerDistance;
    this.levelsPerDeepness = levelsPerDeepness;
    this.randomLevelBonus = randomLevelBonus;
    this.levelPowerPerDistance = levelPowerPerDistance;
    this.levelPowerPerDeepness = levelPowerPerDeepness;
  }

  protected static Optional<BlockPos> readOptionalBlockPos(JsonObject jsonObject, String name) {
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

  protected static Optional<Float> readOptionalFloat(JsonObject jsonObject, String name) {
    if (!jsonObject.has(name)) {
      return Optional.empty();
    } else {
      return Optional.of(jsonObject.get(name).getAsFloat());
    }
  }
}
