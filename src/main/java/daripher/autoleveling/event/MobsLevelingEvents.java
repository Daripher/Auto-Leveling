package daripher.autoleveling.event;

import com.mojang.math.Matrix4f;
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
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
  private static final String LEVEL_TAG = "LEVEL";

  @SubscribeEvent
  public static void applyLevelBonuses(EntityJoinWorldEvent event) {
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
    ResourceKey<Level> dimension = entity.getLevel().dimension();
    DimensionLevelingSettings levelingSettings =
        DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
    return levelingSettings
        .spawnPosOverride()
        .orElse(((ServerLevel) entity.getLevel()).getSharedSpawnPos());
  }

  @SubscribeEvent
  public static void adjustExperienceDrop(LivingExperienceDropEvent event) {
    if (!hasLevel(event.getEntity())) return;
    int level = getLevel(event.getEntityLiving()) + 1;
    int originalExp = event.getDroppedExperience();
    double expBonus = Config.COMMON.expBonus.get() * level;
    event.setDroppedExperience((int) (originalExp + originalExp * expBonus));
  }

  @SubscribeEvent
  public static void dropAdditionalLoot(LivingDropsEvent event) {
    if (!hasLevel(event.getEntity())) return;
    ResourceLocation leveledTableId =
        new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
    MinecraftServer server = event.getEntity().level.getServer();
    if (server == null) return;
    LootTable lootTable = server.getLootTables().get(leveledTableId);
    LootContext lootContext = createLootContext(event.getEntityLiving(), event.getSource());
    lootTable.getRandomItems(lootContext).forEach(event.getEntity()::spawnAtLocation);
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
    NetworkDispatcher.networkChannel.send(
        PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()),
        new SyncLevelingData(entity));
  }

  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void renderEntityLevel(RenderNameplateEvent event) {
    if (!(event.getEntity() instanceof LivingEntity entity)) return;
    if (!shouldShowName(entity)) return;
    Minecraft minecraft = Minecraft.getInstance();
    event.setResult(Event.Result.ALLOW);
    double distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
    if (!ForgeHooksClient.isNameplateInRenderDistance(entity, distance)) return;
    int entityLevel = getLevel(entity) + 1;
    Component entityName = event.getContent();
    MutableComponent levelString =
        new TranslatableComponent("autoleveling.level", entityLevel)
            .withStyle(ChatFormatting.GREEN);
    float textY = entity.getBbHeight() + 0.5F;
    int textOffsetY = "deadmau5".equals(entityName.getString()) ? -10 : 0;
    event.getPoseStack().pushPose();
    event.getPoseStack().translate(0.0D, textY, 0.0D);
    event.getPoseStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
    event.getPoseStack().scale(-0.025F, -0.025F, 0.025F);
    Matrix4f pose = event.getPoseStack().last().pose();
    float backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
    int alpha = (int) (backgroundOpacity * 255.0F) << 24;
    Font font = minecraft.font;
    int textX = -font.width(entityName) / 2 - 5 - font.width(levelString);
    MultiBufferSource multiBufferSource = event.getMultiBufferSource();
    int packedLight = event.getPackedLight();
    font.drawInBatch(
        levelString,
        textX,
        textOffsetY,
        553648127,
        false,
        pose,
        multiBufferSource,
        !entity.isDiscrete(),
        alpha,
        packedLight);
    if (!entity.isDiscrete())
      font.drawInBatch(
          levelString,
          textX,
          textOffsetY,
          -1,
          false,
          pose,
          multiBufferSource,
          false,
          0,
          packedLight);
    event.getPoseStack().popPose();
  }

  @OnlyIn(Dist.CLIENT)
  private static boolean shouldShowName(LivingEntity entity) {
    if (!hasLevel(entity)) return false;
    if (!shouldShowLevel(entity)) return false;
    Minecraft minecraft = Minecraft.getInstance();
    Boolean alwaysShowLevel = Config.COMMON.alwaysShowLevel.get();
    Boolean showLevelWhenLookingAt = Config.COMMON.showLevelWhenLookingAt.get();
    if (!alwaysShowLevel && !(showLevelWhenLookingAt && minecraft.crosshairPickEntity == entity))
      return false;
    LocalPlayer clientPlayer = minecraft.player;
    if (clientPlayer == null) return false;
    return Minecraft.renderNames()
        && entity != minecraft.getCameraEntity()
        && !entity.isInvisibleTo(clientPlayer)
        && !entity.isVehicle()
        && clientPlayer.hasLineOfSight(entity);
  }

  private static boolean shouldSetLevel(Entity entity) {
    if (entity.level.isClientSide) return false;
    return canHaveLevel(entity);
  }

  private static int createLevelForEntity(LivingEntity entity, double distanceFromSpawn) {
    MinecraftServer server = entity.getServer();
    if (server == null) return 0;
    LevelingSettings levelingSettings =
        EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
    if (levelingSettings == null) {
      ResourceKey<Level> dimension = entity.level.dimension();
      levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
    }
    int monsterLevel = (int) (levelingSettings.levelsPerDistance() * distanceFromSpawn);
    int maxLevel = levelingSettings.maxLevel();
    int levelBonus = levelingSettings.randomLevelBonus() + 1;
    monsterLevel += levelingSettings.startingLevel() - 1;
    if (levelBonus > 0) monsterLevel += entity.getRandom().nextInt(levelBonus);
    monsterLevel = Math.abs(monsterLevel);
    monsterLevel += WorldLevelingData.get((ServerLevel) entity.level).getLevelBonus();
    if (maxLevel > 0) monsterLevel = Math.min(monsterLevel, maxLevel - 1);
    GlobalLevelingData globalLevelingData = GlobalLevelingData.get(server);
    monsterLevel += globalLevelingData.getLevelBonus();
    if (entity.getY() < 64) {
      double deepness = 64 - entity.getY();
      monsterLevel += (int) (levelingSettings.levelsPerDeepness() * deepness);
    }
    return monsterLevel;
  }

  @SubscribeEvent
  public static void applyDamageBonus(LivingHurtEvent event) {
    DamageSource damageSource = event.getSource();
    if (!(damageSource.getEntity() instanceof LivingEntity attacker)) return;
    if (damageSource.isProjectile()) {
      Attribute projectileDamage = AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get();
      if (attacker.getAttribute(projectileDamage) == null) return;
      float damageBonus = (float) attacker.getAttributeValue(projectileDamage);
      event.setAmount(event.getAmount() * damageBonus);
    }
    if (damageSource.isExplosion()) {
      Attribute explosionDamage = AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get();
      if (attacker.getAttribute(explosionDamage) == null) return;
      float damageBonus = (float) attacker.getAttributeValue(explosionDamage);
      event.setAmount(event.getAmount() * damageBonus);
    }
  }

  public static void applyAttributeBonuses(LivingEntity entity) {
    int level = getLevel(entity);
    Config.getAttributeBonuses()
        .forEach(
            (attribute, bonus) -> applyAttributeBonusIfPossible(entity, attribute, bonus * level));
  }

  private static void applyAttributeBonusIfPossible(
      LivingEntity entity, Attribute attribute, double bonus) {
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
    MinecraftServer server = entity.getLevel().getServer();
    if (server == null) return;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      LootTable equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
      if (equipmentTable == LootTable.EMPTY) continue;
      LootContext lootContext = createEquipmentLootContext(entity);
      equipmentTable
          .getRandomItems(lootContext)
          .forEach(itemStack -> entity.setItemSlot(slot, itemStack));
    }
  }

  private static LootTable getEquipmentLootTableForSlot(
      MinecraftServer server, LivingEntity entity, EquipmentSlot equipmentSlot) {
    ResourceLocation entityId = EntityType.getKey(entity.getType());
    ResourceLocation lootTableId =
        new ResourceLocation(
            entityId.getNamespace(),
            "equipment/" + entityId.getPath() + "_" + equipmentSlot.getName());
    return server.getLootTables().get(lootTableId);
  }

  private static LootContext createLootContext(LivingEntity entity, DamageSource damageSource) {
    LivingEntityAccessor accessor = (LivingEntityAccessor) entity;
    int lastHurtByPlayerTime = accessor.getLastHurtByPlayerTime();
    Builder builder = accessor.invokeCreateLootContext(lastHurtByPlayerTime > 0, damageSource);
    return builder.create(LootContextParamSets.ENTITY);
  }

  private static LootContext createEquipmentLootContext(LivingEntity entity) {
    return new Builder((ServerLevel) entity.level)
        .withRandom(entity.getRandom())
        .withParameter(LootContextParams.THIS_ENTITY, entity)
        .withParameter(LootContextParams.ORIGIN, entity.position())
        .create(LootContextParamSets.SELECTOR);
  }

  private static boolean canHaveLevel(Entity entity) {
    if (!(entity instanceof LivingEntity)) return false;
    if (entity.getType() == EntityType.PLAYER) return false;
    ResourceLocation entityId = EntityType.getKey(entity.getType());
    String entityNamespace = entityId.getNamespace();
    List<String> blacklistedMobs = Config.COMMON.blacklistedMobs.get();
    if (blacklistedMobs.contains(entityNamespace + ":*")) return false;
    List<String> whitelistedMobs = Config.COMMON.whitelistedMobs.get();
    if (whitelistedMobs.contains(entityNamespace + ":*")) return true;
    if (blacklistedMobs.contains(entityId.toString())) return false;
    if (!whitelistedMobs.isEmpty()) return whitelistedMobs.contains(entityId.toString());
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
