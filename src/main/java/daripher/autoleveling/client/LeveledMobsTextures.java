package daripher.autoleveling.client;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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
import org.slf4j.Logger;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public enum LeveledMobsTextures implements ResourceManagerReloadListener {
  INSTANCE;

  private static final Map<EntityType<?>, Map<Integer, ResourceLocation>> CACHED_TEXTURES =
      new HashMap<>();
  private static final String TEXTURES_FOLDER = "textures/leveled_mobs";
  private static final String TEXTURE_FILE_NAME_FORMAT = "^[a-z|_]+_[1-9]+$";
  private static final String PNG_FILE_SUFFIX = ".png";
  private static final Logger LOGGER = LogUtils.getLogger();

  @Nullable
  public static ResourceLocation get(EntityType<?> entityType, int level) {
    if (!hasTexturesFor(entityType)) {
      return null;
    }
    for (int i = level; i > 0; i--) {
      ResourceLocation textureLocation = getTextureFor(entityType, i);
      if (textureLocation != null) {
        return textureLocation;
      }
    }
    return null;
  }

  @Nullable
  private static ResourceLocation getTextureFor(EntityType<?> entityType, int level) {
    return CACHED_TEXTURES.get(entityType).get(level);
  }

  private static boolean hasTexturesFor(EntityType<?> entityType) {
    return CACHED_TEXTURES.containsKey(entityType) && !CACHED_TEXTURES.get(entityType).isEmpty();
  }

  @SubscribeEvent
  public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(INSTANCE);
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    CACHED_TEXTURES.clear();
    Set<ResourceLocation> textures = resourceManager.listResources(TEXTURES_FOLDER, this::isPngImage).keySet();
    Predicate<String> textureNamePredicate = Pattern.compile(TEXTURE_FILE_NAME_FORMAT).asPredicate();
    Stream<ResourceLocation> validTextures =
        textures.stream()
            .filter(textureLocation -> textureNamePredicate.test(textureLocation.toString()));
    validTextures.forEach(this::saveTexture);
  }

  private void saveTexture(ResourceLocation textureLocation) {
    String textureName = getTextureFileName(textureLocation);
    String[] splitTextureName = textureName.split("_");
    String entityTypeName = splitTextureName[0];
    ResourceLocation entityTypeId = new ResourceLocation(textureLocation.getNamespace(), entityTypeName);
    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityTypeId);
    if (entityType == null) {
      LOGGER.warn(
          "Can't read texture {}, unknown entity type {} specified", textureLocation, entityTypeId);
      return;
    }
    if (CACHED_TEXTURES.get(entityType) == null) {
      CACHED_TEXTURES.put(entityType, new HashMap<>());
    }
    int entityLevel = Integer.parseInt(splitTextureName[1]);
    CACHED_TEXTURES.get(entityType).put(entityLevel, textureLocation);
  }

  public String getTextureFileName(ResourceLocation resourceLocation) {
    return resourceLocation.getPath().replace(TEXTURES_FOLDER, "").replace(PNG_FILE_SUFFIX, "");
  }

  private boolean isPngImage(ResourceLocation resourceLocation) {
    return resourceLocation.getPath().endsWith(PNG_FILE_SUFFIX);
  }
}
