package daripher.autoleveling.client;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

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
public enum LeveledMobsTextures implements ResourceManagerReloadListener {
	INSTANCE;

	private static final Map<EntityType<?>, Map<Integer, ResourceLocation>> CACHED_TEXTURES = new HashMap<>();
	private static final String TEXTURES_FOLDER = "textures/leveled_mobs";
	private static final String TEXTURE_FILE_NAME_FORMAT = "^[a-z|_]+_[1-9]+$";
	private static final String PNG_FILE_SUFFIX = ".png";
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		CACHED_TEXTURES.clear();
		var textures = resourceManager.listResources(TEXTURES_FOLDER, this::isPngImage).keySet();
		var textureNamePredicate = Pattern.compile(TEXTURE_FILE_NAME_FORMAT).asPredicate();
		var validTextures = textures.stream().filter(textureLocation -> textureNamePredicate.test(textureLocation.toString()));
		validTextures.forEach(this::saveTexture);
	}

	private void saveTexture(ResourceLocation textureLocation) {
		var textureName = getTextureFileName(textureLocation);
		var splitTextureName = textureName.split("_");
		var entityTypeName = splitTextureName[0];
		var entityTypeId = new ResourceLocation(textureLocation.getNamespace(), entityTypeName);
		var entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityTypeId);
		if (entityType == null) {
			LOGGER.warn("Can't read texture {}, unknown entity type {} specified", textureLocation, entityTypeId);
			return;
		}
		if (CACHED_TEXTURES.get(entityType) == null) {
			CACHED_TEXTURES.put(entityType, new HashMap<>());
		}
		var entityLevel = Integer.parseInt(splitTextureName[1]);
		CACHED_TEXTURES.get(entityType).put(entityLevel, textureLocation);
	}

	public String getTextureFileName(ResourceLocation resourceLocation) {
		return resourceLocation.getPath().replace(TEXTURES_FOLDER, "").replace(PNG_FILE_SUFFIX, "");
	}

	@Nullable
	public static ResourceLocation get(EntityType<?> entityType, int level) {
		if (!hasTexturesFor(entityType)) {
			return null;
		}
		for (int i = level; i > 0; i--) {
			var textureLocation = getTextureFor(entityType, i);
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

	private boolean isPngImage(ResourceLocation resourceLocation) {
		return resourceLocation.getPath().endsWith(PNG_FILE_SUFFIX);
	}

	@SubscribeEvent
	public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(INSTANCE);
	}
}
