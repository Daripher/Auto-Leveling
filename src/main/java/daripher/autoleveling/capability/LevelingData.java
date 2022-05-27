package daripher.autoleveling.capability;

import daripher.autoleveling.api.ILevelingData;
import net.minecraft.nbt.CompoundTag;

public class LevelingData implements ILevelingData
{
	private int level;
	
	public int getLevel()
	{
		return level;
	}
	
	public void setLevel(int level)
	{
		this.level = level;
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putInt("level", level);
		return compoundTag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag compoundTag)
	{
		level = compoundTag.getInt("level");
	}
}
