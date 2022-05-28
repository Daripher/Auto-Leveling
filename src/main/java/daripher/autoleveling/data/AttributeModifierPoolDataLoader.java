package daripher.autoleveling.data;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class AttributeModifierPoolDataLoader implements ResourceManagerReloadListener
{
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final AttributeModifierPoolDataLoader INSTANCE = new AttributeModifierPoolDataLoader();
	
	@SubscribeEvent
	public static void onAddReloadListener(AddReloadListenerEvent event)
	{
		event.addListener(INSTANCE);
	}
	
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager)
	{
		for (ResourceLocation id : resourceManager.listResources("attribute_modifier_pools", s -> s.endsWith(".json")))
		{
			ResourceLocation poolId = new ResourceLocation(id.getNamespace(), id.getPath().replace("attribute_modifier_pools/", "").replace(".json", ""));
			
			try
			{
				Resource resource = resourceManager.getResource(id);
				
				try (InputStream stream = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(stream))
				{
					JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
					LOGGER.debug("Reading attribute modifier pool " + poolId);
					new AttributeModifierPool(poolId, jsonObject);
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("Error reading attribute modifier pool " + poolId + "!", e);
			}
		}
	}
}
