package daripher.autoleveling.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class GlobalLevelingData extends SavedData
{
	private int levelBonus;
	
	@Override
	public CompoundTag save(CompoundTag tag)
	{
		tag.putInt("LevelBonus", levelBonus);
		return tag;
	}
	
	public void setLevel(int level)
	{
		this.levelBonus = level;
		setDirty();
	}
	
	public int getLevelBonus()
	{
		return levelBonus;
	}
	
	private static GlobalLevelingData create()
	{
		return new GlobalLevelingData();
	}
	
	private static GlobalLevelingData load(CompoundTag tag)
	{
		GlobalLevelingData data = create();
		data.levelBonus = tag.getInt("LevelBonus");
		return data;
	}
	
	public static GlobalLevelingData get(MinecraftServer server)
	{
		return server.overworld().getDataStorage().computeIfAbsent(GlobalLevelingData::load, GlobalLevelingData::create, "global_leveling");
	}
}
