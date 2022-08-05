package daripher.autoleveling.data;

import com.google.gson.JsonObject;

public record LevelingSettings(int startingLevel, int maxLevel, float levelsPerDistance, int randomLevelBonus)
{	
	public static final LevelingSettings DEFAULT = new LevelingSettings(1, 0, 0.01F, 0);
	
	public static LevelingSettings load(JsonObject jsonObject)
	{
		int startingLevel = jsonObject.get("starting_level").getAsInt();
		int maxLevel = jsonObject.get("max_level").getAsInt();
		float levelsPerDistance = jsonObject.get("levels_per_distance").getAsFloat();
		int randomLevelBonus = jsonObject.get("random_level_bonus").getAsInt();
		return new LevelingSettings(startingLevel, maxLevel, levelsPerDistance, randomLevelBonus);
	}
}
