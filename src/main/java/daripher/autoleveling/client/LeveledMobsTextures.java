package daripher.autoleveling.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class LeveledMobsTextures implements ResourceManagerReloadListener
{
	private static final LeveledMobsTextures INSTANCE = new LeveledMobsTextures();
	private static final Map<EntityType<?>, Map<Integer, ResourceLocation>> TEXTURES = new HashMap<>();
	
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager)
	{
		TEXTURES.clear();
		Collection<ResourceLocation> entityTextures = resourceManager.listResources("textures/leveled_mobs", s -> s.endsWith(".png"));
		
		if (!entityTextures.isEmpty())
		{
			for (ResourceLocation location : entityTextures)
			{
				String fileName = location.getPath().replace("textures/leveled_mobs/", "").replace(".png", "");
				
				if (!fileName.contains("_"))
					continue;
				
				ResourceLocation entityId = new ResourceLocation(location.getNamespace(), fileName.split("_")[0]);
				EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);
				
				if (entityType == null)
					continue;
				
				if (TEXTURES.get(entityType) == null)
					TEXTURES.put(entityType, new HashMap<>());
				
				try
				{
					int level = Integer.parseInt(fileName.split("_")[1]);
					TEXTURES.get(entityType).put(level, location);
				}
				catch (NumberFormatException exception)
				{
					exception.printStackTrace();
				}
			}
		}
	}
	
	@Nullable
	public static ResourceLocation get(EntityType<?> entityType, int level)
	{
		if (!hasTextures(entityType))
		{
			return null;
		}
		
		for (int i = level; i > 0; i--)
		{
			ResourceLocation texture = TEXTURES.get(entityType).get(i);
			
			if (texture != null)
			{
				return texture;
			}
		}
		
		return null;
	}
	
	private static boolean hasTextures(EntityType<?> entityType)
	{
		return TEXTURES.containsKey(entityType) && !TEXTURES.get(entityType).isEmpty();
	}
	
	@SubscribeEvent
	public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event)
	{
		event.registerReloadListener(INSTANCE);
	}
}
