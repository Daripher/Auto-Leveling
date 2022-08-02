package daripher.autoleveling.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

public class LevelingSettings
{
	public static final LevelingSettings DEFAULT = new LevelingSettings(1, 0, 0.01F, 0);
	
	public final int startingLevel;
	public final int maxLevel;
	public final float levelsPerDistance;
	public final int randomLevelBonus;
	
	public LevelingSettings(int startingLevel, int maxLevel, float levelsPerDistance, int randomLevelBonus)
	{
		this.startingLevel = startingLevel;
		this.maxLevel = maxLevel;
		this.levelsPerDistance = levelsPerDistance;
		this.randomLevelBonus = randomLevelBonus;
	}
	
	public static LevelingSettings load(Gson gson, ResourceLocation id, JsonElement json, LevelingSettingsReloader levelingSettingsReloader)
	{
		JsonObject jsonObject = json.getAsJsonObject();
		int startingLevel = jsonObject.get("starting_level").getAsInt();
		int maxLevel = jsonObject.get("max_level").getAsInt();
		float levelsPerDistance = jsonObject.get("levels_per_distance").getAsFloat();
		int randomLevelBonus = jsonObject.get("random_level_bonus").getAsInt();
		return new LevelingSettings(startingLevel, maxLevel, levelsPerDistance, randomLevelBonus);
	}
}
