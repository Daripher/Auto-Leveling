package daripher.autoleveling.saveddata;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class GlobalLevelingData extends WorldSavedData {
	private int levelBonus;

	public GlobalLevelingData() {
		super("global_leveling");
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag.putInt("LevelBonus", levelBonus);
		return tag;
	}

	@Override
	public void load(CompoundNBT tag) {
		levelBonus = tag.getInt("LevelBonus");
	}

	public void setLevel(int level) {
		this.levelBonus = level;
		setDirty();
	}

	public int getLevelBonus() {
		return levelBonus;
	}

	private static GlobalLevelingData create() {
		return new GlobalLevelingData();
	}

	public static GlobalLevelingData get(MinecraftServer server) {
		DimensionSavedDataManager dataStorage = server.overworld().getDataStorage();
		GlobalLevelingData data = dataStorage.get(GlobalLevelingData::create, "global_leveling");

		if (data == null) {
			dataStorage.set(data = GlobalLevelingData.create());
		}

		return data;
	}
}
