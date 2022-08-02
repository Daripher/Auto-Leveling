package daripher.autoleveling.data;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.loot.LootSerializers;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class LevelingSettingsReloader extends JsonReloadListener
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = LootSerializers.createLootTableSerializer().create();
	private static Map<ResourceLocation, LevelingSettings> settings = ImmutableMap.of();
	
	public LevelingSettingsReloader()
	{
		super(GSON, "leveling_settings");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler)
	{
		ImmutableMap.Builder<ResourceLocation, LevelingSettings> builder = ImmutableMap.builder();
		
		map.forEach((id, json) ->
		{
			try
			{
				LevelingSettings levelingSettings = LevelingSettings.load(GSON, id, json, this);
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
	
	public static LevelingSettings getSettingsForDimension(RegistryKey<World> dimension)
	{
		return settings.getOrDefault(dimension.location(), LevelingSettings.DEFAULT);
	}
}
