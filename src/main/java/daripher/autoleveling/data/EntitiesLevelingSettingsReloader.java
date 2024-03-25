package daripher.autoleveling.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import daripher.autoleveling.settings.EntityLevelingSettings;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class EntitiesLevelingSettingsReloader extends SimpleJsonResourceReloadListener {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Gson GSON = Deserializers.createLootTableSerializer().create();
  private static final Map<ResourceLocation, EntityLevelingSettings> SETTINGS = new HashMap<>();

  public EntitiesLevelingSettingsReloader() {
    super(GSON, "leveling_settings/entities");
  }

  @Nullable
  public static EntityLevelingSettings get(EntityType<?> entityType) {
    return SETTINGS.get(ForgeRegistries.ENTITY_TYPES.getKey(entityType));
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> jsonElements,
      @NotNull ResourceManager resourceManager,
      @NotNull ProfilerFiller profilerFiller) {
    SETTINGS.clear();
    jsonElements.forEach(this::loadSettings);
  }

  private void loadSettings(ResourceLocation fileId, JsonElement jsonElement) {
    try {
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      EntityLevelingSettings settings = EntityLevelingSettings.load(jsonObject);
      SETTINGS.put(fileId, settings);
      LOGGER.info("Loaded leveling settings {}", fileId);
    } catch (Exception exception) {
      LOGGER.error("Couldn't load leveling settings {}", fileId);
      exception.printStackTrace();
    }
  }
}
