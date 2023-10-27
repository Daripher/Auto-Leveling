package daripher.autoleveling.settings;

public interface LevelingSettings {
  int startingLevel();

  int maxLevel();

  float levelsPerDistance();

  float levelsPerDeepness();

  int randomLevelBonus();
}
