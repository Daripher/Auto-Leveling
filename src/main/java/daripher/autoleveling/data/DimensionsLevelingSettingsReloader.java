package daripher.autoleveling.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.settings.DimensionLevelingSettings;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.Deserializers;

public class DimensionsLevelingSettingsReloader extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = Deserializers.createLootTableSerializer().create();
	private static final Map<ResourceLocation, DimensionLevelingSettings> SETTINGS = new HashMap<>();

	public DimensionsLevelingSettingsReloader() {
		super(GSON, "leveling_settings/dimensions");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		SETTINGS.clear();
		map.forEach(this::loadSettings);
	}

	private void loadSettings(ResourceLocation fileId, JsonElement jsonElement) {
		try {
			LOGGER.info("Loading leveling settings {}", fileId);
			DimensionLevelingSettings settings = DimensionLevelingSettings.load(jsonElement.getAsJsonObject());
			SETTINGS.put(fileId, settings);
		} catch (Exception exception) {
			LOGGER.error("Couldn't parse leveling settings {}", fileId, exception);
		}
	}

	@Nonnull
	public static DimensionLevelingSettings getSettingsForDimension(ResourceKey<Level> dimension) {
		return SETTINGS.getOrDefault(dimension.location(), createDefaultSettings());
	}

	private static DimensionLevelingSettings createDefaultSettings() {
		int startingLevel = Config.COMMON.defaultStartingLevel.get();
		int maxLevel = Config.COMMON.defaultMaxLevel.get();
		float levelPerDistance = Config.COMMON.defaultLevelsPerDistance.get().floatValue();
		float levelPerDeepness = Config.COMMON.defaultLevelsPerDeepness.get().floatValue();
		int randomLevelBonus = Config.COMMON.defaultRandomLevelBonus.get();
		return new DimensionLevelingSettings(startingLevel, maxLevel, levelPerDistance, levelPerDeepness, randomLevelBonus, Optional.empty());
	}
}
