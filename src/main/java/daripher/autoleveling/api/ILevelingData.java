package daripher.autoleveling.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface ILevelingData extends INBTSerializable<CompoundTag> {
	int getLevel();

	void setLevel(int level);
}
