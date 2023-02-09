package daripher.autoleveling.data;

import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;

import daripher.autoleveling.config.Config;
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
	private static Map<ResourceLocation, LevelingSettings> settings = ImmutableMap.of();

	public DimensionsLevelingSettingsReloader() {
		super(GSON, "leveling_settings/dimensions");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		ImmutableMap.Builder<ResourceLocation, LevelingSettings> builder = ImmutableMap.builder();

		map.forEach((id, json) -> {
			try {
				var levelingSettings = LevelingSettings.load(json.getAsJsonObject());
				builder.put(id, levelingSettings);
				LOGGER.info("Loading leveling settings {}", id);
			} catch (Exception exception) {
				LOGGER.error("Couldn't parse leveling settings {}", id, exception);
			}
		});

		settings = builder.build();
	}

	@Nonnull
	public static LevelingSettings getSettingsForDimension(ResourceKey<Level> dimension) {
		return settings.getOrDefault(dimension.location(), defaultSettings());
	}

	private static LevelingSettings defaultSettings() {
		var startingLevel = Config.COMMON.defaultStartingLevel.get();
		var maxLevel = Config.COMMON.defaultMaxLevel.get();
		var levelPerDistance = Config.COMMON.defaultLevelsPerDistance.get().floatValue();
		var randomLevelBonus = Config.COMMON.defaultRandomLevelBonus.get();
		return new LevelingSettings(startingLevel, maxLevel, levelPerDistance, randomLevelBonus);
	}
}
