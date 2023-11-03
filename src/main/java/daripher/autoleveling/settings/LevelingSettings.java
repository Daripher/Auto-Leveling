package daripher.autoleveling.settings;

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
}
