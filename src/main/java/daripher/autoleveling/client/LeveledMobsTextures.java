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
import org.codehaus.plexus.util.StringUtils;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class LeveledMobsTextures implements ISelectiveResourceReloadListener {
  private static final LeveledMobsTextures INSTANCE = new LeveledMobsTextures();
  private static final Map<EntityType<?>, Map<Integer, ResourceLocation>> TEXTURES =
      new HashMap<>();

  @Nullable
  public static ResourceLocation get(EntityType<?> entityType, int level) {
    if (!hasTextures(entityType)) {
      return null;
    }

    for (int i = level; i > 0; i--) {
      ResourceLocation texture = TEXTURES.get(entityType).get(i);

      if (texture != null) {
        return texture;
      }
    }

    return null;
  }

  private static boolean hasTextures(EntityType<?> entityType) {
    return TEXTURES.containsKey(entityType) && !TEXTURES.get(entityType).isEmpty();
  }

  @SubscribeEvent
  public static void onModelRegistry(ModelRegistryEvent event) {
    IReloadableResourceManager resourceManager =
        (IReloadableResourceManager) Minecraft.getInstance().getResourceManager();
    resourceManager.registerReloadListener(INSTANCE);
  }

  @SubscribeEvent
  public static void onClientSetup(FMLClientSetupEvent event) {
    INSTANCE.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
  }

  @Override
  public void onResourceManagerReload(
      IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
    if (!resourcePredicate.test(VanillaResourceType.TEXTURES)) return;

    TEXTURES.clear();
    Collection<ResourceLocation> leveledMobsTextures =
        resourceManager.listResources("textures/leveled_mobs", s -> s.endsWith(".png"));

    if (!leveledMobsTextures.isEmpty()) {
      reloadLeveledMobsTextures(leveledMobsTextures);
    }
  }

  private void reloadLeveledMobsTextures(Collection<ResourceLocation> leveledMobsTextures) {
    for (ResourceLocation textureLocation : leveledMobsTextures) {
      String fileName =
          textureLocation.getPath().replace("textures/leveled_mobs/", "").replace(".png", "");

      if (!fileName.contains("_")) continue;

      String entityId = fileName.split("_")[0];
      EntityType<?> entityType =
          ForgeRegistries.ENTITIES.getValue(
              new ResourceLocation(textureLocation.getNamespace(), entityId));

      if (entityType == null) continue;

      if (TEXTURES.get(entityType) == null) TEXTURES.put(entityType, new HashMap<>());

      String levelString = fileName.split("_")[1];

      if (levelString.isEmpty() || !StringUtils.isNumeric(levelString)) return;

      int level = Integer.parseInt(levelString);
      TEXTURES.get(entityType).put(level, textureLocation);
    }
  }
}
