package daripher.autoleveling.data;

import com.google.gson.JsonObject;

public record LevelingSettings(int startingLevel, int maxLevel, float levelsPerDistance, int randomLevelBonus) {
	public static LevelingSettings load(JsonObject jsonObject) {
		var startingLevel = jsonObject.get("starting_level").getAsInt();
		var maxLevel = jsonObject.get("max_level").getAsInt();
		var levelsPerDistance = jsonObject.get("levels_per_distance").getAsFloat();
		var randomLevelBonus = jsonObject.get("random_level_bonus").getAsInt();
		return new LevelingSettings(startingLevel, maxLevel, levelsPerDistance, randomLevelBonus);
	}
}
