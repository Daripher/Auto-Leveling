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
		
		ForgeRegistries.ENTITIES.getValues().forEach(entityType ->
		{
			ResourceLocation entityId = ForgeRegistries.ENTITIES.getKey(entityType);
			Predicate<String> textureFilter = s ->
			{
				ResourceLocation l = new ResourceLocation(s);
				return l.getPath().startsWith(entityId.getPath() + "_") && l.getPath().endsWith(".png") && l.getNamespace().equals(entityId.getNamespace());
			};
			Collection<ResourceLocation> entityTextures = resourceManager.listResources("textures/leveled_mobs", textureFilter);
			
			if (!entityTextures.isEmpty())
			{
				TEXTURES.put(entityType, new HashMap<>());
				
				entityTextures.forEach(location ->
				{
					try
					{
						String textureNameStart = "textures/leveled_mobs/" + entityId.getPath() + "_";
						int level = Integer.parseInt(location.getPath().replace(textureNameStart, "").replace(".png", ""));
						TEXTURES.get(entityType).put(level, location);
					}
					catch (NumberFormatException e)
					{
					}
				});
			}
		});
		
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
