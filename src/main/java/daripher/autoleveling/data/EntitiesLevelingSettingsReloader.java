package daripher.autoleveling.data;

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

public class EntitiesLevelingSettingsReloader extends SimpleJsonResourceReloadListener
{
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = Deserializers.createLootTableSerializer().create();
	private static Map<ResourceLocation, LevelingSettings> settings = ImmutableMap.of();
	
	public EntitiesLevelingSettingsReloader()
	{
		super(GSON, "leveling_settings/entities");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller)
	{
		ImmutableMap.Builder<ResourceLocation, LevelingSettings> builder = ImmutableMap.builder();
		
		map.forEach((id, json) ->
		{
			try
			{
				LevelingSettings levelingSettings = LevelingSettings.load(json.getAsJsonObject());
				builder.put(id, levelingSettings);
				LOGGER.info("Loading leveling settings {}", id);
			}
			catch (Exception exception)
			{
				LOGGER.error("Couldn't parse leveling settings {}", id, exception);
			}
		});
		
		ImmutableMap<ResourceLocation, LevelingSettings> immutableMap = builder.build();
		settings = immutableMap;
	}
	
	@Nullable
	public static LevelingSettings getSettingsForEntity(EntityType<?> entityType)
	{
		return settings.get(ForgeRegistries.ENTITY_TYPES.getKey(entityType));
	}
}