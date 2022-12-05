package daripher.autoleveling.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class LeveledMobsTextures implements ISelectiveResourceReloadListener
{
	private static final LeveledMobsTextures INSTANCE = new LeveledMobsTextures();
	private static final Map<EntityType<?>, Map<Integer, ResourceLocation>> TEXTURES = new HashMap<>();
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if (!resourcePredicate.test(VanillaResourceType.TEXTURES))
			return;
		
		TEXTURES.clear();
		Collection<ResourceLocation> entityTextures = resourceManager.listResources("textures/leveled_mobs", s -> s.endsWith(".png"));
		
		if (!entityTextures.isEmpty())
		{
			for (ResourceLocation location : entityTextures)
			{
				String fileName = location.getPath().replace("textures/leveled_mobs/", "").replace(".png", "");
				
				if (!fileName.contains("_"))
					continue;
				
				String entityId = fileName.split("_")[0];
				EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(location.getNamespace(), entityId));
				
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
	public static void onModelRegistry(ModelRegistryEvent event)
	{
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(INSTANCE);
	}
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		INSTANCE.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
	}
}
