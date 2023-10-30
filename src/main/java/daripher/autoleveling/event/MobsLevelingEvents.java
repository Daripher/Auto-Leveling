package daripher.autoleveling.event;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.data.EntitiesLevelingSettingsReloader;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.mixin.LivingEntityAccessor;
import daripher.autoleveling.network.NetworkDispatcher;
import daripher.autoleveling.network.message.SyncLevelingData;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import daripher.autoleveling.saveddata.WorldLevelingData;
import daripher.autoleveling.settings.DimensionLevelingSettings;
import daripher.autoleveling.settings.LevelingSettings;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
  private static final String LEVEL_TAG = "LEVEL";

  @SubscribeEvent
  public static void applyLevelBonuses(EntityJoinLevelEvent event) {
    if (!shouldSetLevel(event.getEntity())) return;
    LivingEntity entity = (LivingEntity) event.getEntity();
    if (event.loadedFromDisk() && hasLevel(entity)) return;
    BlockPos spawnPos = getSpawnPosition(entity);
    double distanceToSpawn = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
    int level = createLevelForEntity(entity, distanceToSpawn);
    setLevel(entity, level);
    applyAttributeBonuses(entity);
    addEquipment(entity);
  }

  private static BlockPos getSpawnPosition(LivingEntity entity) {
    ResourceKey<Level> dimension = entity.level().dimension();
    DimensionLevelingSettings levelingSettings =
        DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
    return levelingSettings.spawnPosOverride().orElse(entity.level().getSharedSpawnPos());
  }

  @SubscribeEvent
  public static void adjustExperienceDrop(LivingExperienceDropEvent event) {
    if (!hasLevel(event.getEntity())) return;
    int level = getLevel(event.getEntity()) + 1;
    int originalExp = event.getDroppedExperience();
    double expBonus = Config.COMMON.expBonus.get() * level;
    event.setDroppedExperience((int) (originalExp + originalExp * expBonus));
  }

  @SubscribeEvent
  public static void dropAdditionalLoot(LivingDropsEvent event) {
    LivingEntity entity = event.getEntity();
    if (!hasLevel(entity)) return;
    ResourceLocation lootTableId =
        new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
    MinecraftServer server = entity.level().getServer();
    if (server == null) return;
    LootTable lootTable = server.getLootData().getLootTable(lootTableId);
    LootParams lootParams = createLootParams(entity, event.getSource());
    lootTable.getRandomItems(lootParams, entity::spawnAtLocation);
  }

  @SubscribeEvent
  public static void reloadSettings(AddReloadListenerEvent event) {
    event.addListener(new DimensionsLevelingSettingsReloader());
    event.addListener(new EntitiesLevelingSettingsReloader());
  }

  @SubscribeEvent
  public static void syncEntityLevel(PlayerEvent.StartTracking event) {
    if (!hasLevel(event.getTarget())) return;
    LivingEntity entity = (LivingEntity) event.getTarget();
    NetworkDispatcher.network_channel.send(
        PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()),
        new SyncLevelingData(entity));
  }

  @SubscribeEvent
  public static void applyAttributesDamageBonus(LivingHurtEvent event) {
    DamageSource damage = event.getSource();
    if (!(damage.getEntity() instanceof LivingEntity attacker)) return;
    float multiplier = getDamageMultiplier(damage, attacker);
    if (multiplier > 1F) event.setAmount(event.getAmount() * multiplier);
  }

  public static float getDamageMultiplier(DamageSource damage, LivingEntity attacker) {
    if (damage.is(DamageTypeTags.IS_PROJECTILE)) {
      return getAttributeValue(attacker, AutoLevelingAttributes.PROJECTILE_DAMAGE_MULTIPLIER.get());
    }
    if (damage.is(DamageTypeTags.IS_EXPLOSION)) {
      return getAttributeValue(attacker, AutoLevelingAttributes.EXPLOSION_DAMAGE_MULTIPLIER.get());
    }
    return 1F;
  }

  private static float getAttributeValue(LivingEntity entity, Attribute damageBonusAttribute) {
    if (entity.getAttribute(damageBonusAttribute) == null) return 0F;
    return (float) Objects.requireNonNull(entity.getAttribute(damageBonusAttribute)).getValue();
  }

  @OnlyIn(Dist.CLIENT)
  public static boolean shouldShowName(LivingEntity entity) {
    if (!Minecraft.renderNames()) return false;
    if (entity.isVehicle()) return false;
    Minecraft minecraft = Minecraft.getInstance();
    if (entity == minecraft.getCameraEntity()) return false;
    LocalPlayer clientPlayer = minecraft.player;
    if (clientPlayer == null) return false;
    if (!clientPlayer.hasLineOfSight(entity) || entity.isInvisibleTo(clientPlayer)) return false;
    if (!hasLevel(entity)) return false;
    if (!shouldShowLevel(entity)) return false;
    if (Config.COMMON.alwaysShowLevel.get()) return true;
    return Config.COMMON.showLevelWhenLookingAt.get() && minecraft.crosshairPickEntity == entity;
  }

  private static boolean shouldSetLevel(Entity entity) {
    if (entity.level().isClientSide) return false;
    return canHaveLevel(entity);
  }

  private static int createLevelForEntity(LivingEntity entity, double distanceFromSpawn) {
    MinecraftServer server = entity.getServer();
    if (server == null) return 0;
    LevelingSettings levelingSettings =
        EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
    if (levelingSettings == null) {
      ResourceKey<Level> dimension = entity.level().dimension();
      levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
    }
    int monsterLevel = (int) (levelingSettings.levelsPerDistance() * distanceFromSpawn);
    int maxLevel = levelingSettings.maxLevel();
    int levelBonus = levelingSettings.randomLevelBonus() + 1;
    monsterLevel += levelingSettings.startingLevel() - 1;
    if (levelBonus > 0) monsterLevel += entity.getRandom().nextInt(levelBonus);
    monsterLevel = Math.abs(monsterLevel);
    monsterLevel += WorldLevelingData.get((ServerLevel) entity.level()).getLevelBonus();
    if (maxLevel > 0) monsterLevel = Math.min(monsterLevel, maxLevel - 1);
    GlobalLevelingData globalLevelingData = GlobalLevelingData.get(server);
    monsterLevel += globalLevelingData.getLevelBonus();
    if (entity.getY() < 64) {
      double deepness = 64 - entity.getY();
      monsterLevel += (int) (levelingSettings.levelsPerDeepness() * deepness);
    }
    return monsterLevel;
  }

  public static void applyAttributeBonuses(LivingEntity entity) {
    int level = getLevel(entity);
    Config.getAttributeBonuses()
        .forEach((attribute, bonus) -> applyAttributeBonus(entity, attribute, bonus * level));
  }

  private static void applyAttributeBonus(LivingEntity entity, Attribute attribute, double bonus) {
    AttributeInstance attributeInstance = entity.getAttribute(attribute);
    if (attributeInstance == null) return;
    UUID modifierId = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
    AttributeModifier modifier = attributeInstance.getModifier(modifierId);
    if (modifier != null && modifier.getAmount() == bonus) return;
    if (modifier != null) attributeInstance.removeModifier(modifier);
    modifier =
        new AttributeModifier(modifierId, "Auto Leveling Bonus", bonus, Operation.MULTIPLY_TOTAL);
    attributeInstance.addPermanentModifier(modifier);
    if (attribute == Attributes.MAX_HEALTH) entity.heal(entity.getMaxHealth());
  }

  public static void addEquipment(LivingEntity entity) {
    MinecraftServer server = entity.level().getServer();
    if (server == null) return;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      LootTable equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
      if (equipmentTable == LootTable.EMPTY) continue;
      LootParams lootParams = createEquipmentLootParams(entity);
      equipmentTable.getRandomItems(lootParams, itemStack -> entity.setItemSlot(slot, itemStack));
    }
  }

  private static LootTable getEquipmentLootTableForSlot(
      MinecraftServer server, LivingEntity entity, EquipmentSlot equipmentSlot) {
    ResourceLocation entityId = EntityType.getKey(entity.getType());
    ResourceLocation lootTableId =
        new ResourceLocation(
            entityId.getNamespace(),
            "equipment/" + entityId.getPath() + "_" + equipmentSlot.getName());
    return server.getLootData().getLootTable(lootTableId);
  }

  private static LootParams createLootParams(LivingEntity entity, DamageSource damageSource) {
    LivingEntityAccessor accessor = (LivingEntityAccessor) entity;
    ServerLevel level = (ServerLevel) entity.level();
    LootParams.Builder builder =
        new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
            .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
            .withOptionalParameter(
                LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());
    int lastHurtByPlayerTime = accessor.getLastHurtByPlayerTime();
    Player lastHurtByPlayer = accessor.getLastHurtByPlayer();
    if (lastHurtByPlayerTime > 0 && lastHurtByPlayer != null) {
      builder =
          builder
              .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, lastHurtByPlayer)
              .withLuck(lastHurtByPlayer.getLuck());
    }
    return builder.create(LootContextParamSets.ENTITY);
  }

  private static LootParams createEquipmentLootParams(LivingEntity entity) {
    return new LootParams.Builder((ServerLevel) entity.level())
        .withParameter(LootContextParams.THIS_ENTITY, entity)
        .withParameter(LootContextParams.ORIGIN, entity.position())
        .create(LootContextParamSets.SELECTOR);
  }

  private static boolean canHaveLevel(Entity entity) {
    if (!(entity instanceof LivingEntity)) return false;
    if (entity.getType() == EntityType.PLAYER) return false;
    ResourceLocation entityId = EntityType.getKey(entity.getType());
    String entityNamespace = entityId.getNamespace();
    List<String> blacklist = Config.COMMON.blacklistedMobs.get();
    if (blacklist.contains(entityNamespace + ":*")) return false;
    List<String> whitelist = Config.COMMON.whitelistedMobs.get();
    if (whitelist.contains(entityNamespace + ":*")) return true;
    if (blacklist.contains(entityId.toString())) return false;
    if (!whitelist.isEmpty()) return whitelist.contains(entityId.toString());
    return true;
  }

  public static boolean shouldShowLevel(Entity entity) {
    ResourceLocation entityId = EntityType.getKey(entity.getType());
    List<String> blacklist = Config.COMMON.blacklistedShownLevels.get();
    if (blacklist.contains(entityId.toString())) return false;
    String namespace = entityId.getNamespace();
    return !blacklist.contains(namespace + ":*");
  }

  public static boolean hasLevel(Entity entity) {
    return entity.getPersistentData().contains(LEVEL_TAG);
  }

  public static int getLevel(LivingEntity entity) {
    return entity.getPersistentData().getInt(LEVEL_TAG);
  }

  public static void setLevel(LivingEntity entity, int level) {
    entity.getPersistentData().putInt(LEVEL_TAG, level);
  }
}
