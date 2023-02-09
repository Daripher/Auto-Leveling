package daripher.autoleveling.data;

import com.google.gson.JsonObject;

public class LevelingSettings {
	public final int startingLevel;
	public final int maxLevel;
	public final float levelsPerDistance;
	public final float levelsPerDeepness;
	public final int randomLevelBonus;

	public LevelingSettings(int startingLevel, int maxLevel, float levelsPerDistance, float levelsPerDeepness, int randomLevelBonus) {
		this.startingLevel = startingLevel;
		this.maxLevel = maxLevel;
		this.levelsPerDistance = levelsPerDistance;
		this.levelsPerDeepness = levelsPerDeepness;
		this.randomLevelBonus = randomLevelBonus;
	}

	public static LevelingSettings load(JsonObject jsonObject) {
		int startingLevel = jsonObject.get("starting_level").getAsInt();
		int maxLevel = jsonObject.get("max_level").getAsInt();
		float levelsPerDistance = jsonObject.get("levels_per_distance").getAsFloat();
		float levelsPerDeepness = jsonObject.get("levels_per_deepness").getAsFloat();
		int randomLevelBonus = jsonObject.get("random_level_bonus").getAsInt();
		return new LevelingSettings(startingLevel, maxLevel, levelsPerDistance, levelsPerDeepness, randomLevelBonus);
	}
}
