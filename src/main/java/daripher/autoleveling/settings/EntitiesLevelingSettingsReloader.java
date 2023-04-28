package daripher.autoleveling.settings;

import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.registries.ForgeRegistries;

public class EntitiesLevelingSettingsReloader extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = Deserializers.createLootTableSerializer().create();
	private static final Map<ResourceLocation, LevelingSettings> SETTINGS = ImmutableMap.of();

	public EntitiesLevelingSettingsReloader() {
		super(GSON, "leveling_settings/entities");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonElements, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		SETTINGS.clear();
		jsonElements.forEach(this::loadSettings);
	}

	private void loadSettings(ResourceLocation fileId, JsonElement jsonElement) {
		try {
			LOGGER.info("Loading leveling settings {}", fileId);
			var settings = LevelingSettings.load(jsonElement.getAsJsonObject());
			SETTINGS.put(fileId, settings);
		} catch (Exception exception) {
			LOGGER.error("Couldn't parse leveling settings {}", fileId, exception);
		}
	}

	@Nullable
	public static LevelingSettings getSettingsForEntity(EntityType<?> entityType) {
		return SETTINGS.get(ForgeRegistries.ENTITY_TYPES.getKey(entityType));
	}
}
