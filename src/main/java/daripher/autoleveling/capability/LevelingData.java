package daripher.autoleveling.capability;

import daripher.autoleveling.api.ILevelingData;

public class LevelingData implements ILevelingData {
	private int level;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
