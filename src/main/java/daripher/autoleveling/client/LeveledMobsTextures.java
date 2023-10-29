package daripher.autoleveling.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
public class LeveledMobsTextures implements ResourceManagerReloadListener {
  private static final LeveledMobsTextures INSTANCE = new LeveledMobsTextures();
  private static final Map<EntityType<?>, Map<Integer, ResourceLocation>> TEXTURES =
      new HashMap<>();

  @Nullable
  public static ResourceLocation get(EntityType<?> entityType, int level) {
    if (!hasTextures(entityType)) return null;
    for (int i = level; i > 0; i--) {
      ResourceLocation textureLocation = TEXTURES.get(entityType).get(i);
      if (textureLocation != null) return textureLocation;
    }
    return null;
  }

  private static boolean hasTextures(EntityType<?> entityType) {
    return TEXTURES.containsKey(entityType) && !TEXTURES.get(entityType).isEmpty();
  }

  @SubscribeEvent
  public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(INSTANCE);
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    TEXTURES.clear();
    Set<ResourceLocation> entityTextures =
        resourceManager
            .listResources("textures/leveled_mobs", l -> l.getPath().endsWith(".png"))
            .keySet();
    if (entityTextures.isEmpty()) return;
    for (ResourceLocation location : entityTextures) {
      String fileName =
          location.getPath().replace("textures/leveled_mobs/", "").replace(".png", "");
      if (!fileName.contains("_")) continue;
      int level;
      try {
        level = Integer.parseInt(fileName.split("_")[1]);
      } catch (NumberFormatException exception) {
        continue;
      }
      String entityId = fileName.split("_")[0];
      EntityType<?> entityType =
          ForgeRegistries.ENTITY_TYPES.getValue(
              new ResourceLocation(location.getNamespace(), entityId));
      if (entityType == null) continue;
      TEXTURES.computeIfAbsent(entityType, k -> new HashMap<>());
      TEXTURES.get(entityType).put(level, location);
    }
  }
}
