package daripher.autoleveling.capability;

import daripher.autoleveling.api.ILevelingData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

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
	
	public static class Storage implements IStorage<ILevelingData>
	{
		@Override
		public CompoundNBT writeNBT(Capability<ILevelingData> capability, ILevelingData instance, Direction side)
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("level", instance.getLevel());
			return nbt;
		}
		
		@Override
		public void readNBT(Capability<ILevelingData> capability, ILevelingData instance, Direction side, INBT inbt)
		{
			CompoundNBT nbt = (CompoundNBT) inbt;
			instance.setLevel(nbt.getInt("level"));
		}
	}	
}
