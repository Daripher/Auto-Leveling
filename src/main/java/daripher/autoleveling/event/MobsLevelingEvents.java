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
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootContext.Builder;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
  private static final String LEVEL_TAG = "LEVEL";

  @SubscribeEvent
  public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
    if (!shouldSetLevel(event.getEntity())) return;
    LivingEntity entity = (LivingEntity) event.getEntity();
    if (hasLevel(entity)) {
      applyAttributeBonuses(entity);
      return;
    }
    BlockPos spawnPos = getSpawnPosition(entity);
    double distanceFromSpawn = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
    int level = createLevelForEntity(entity, distanceFromSpawn);
    setLevel(entity, level);
    applyAttributeBonuses(entity);
    addEquipment(entity);
  }

  private static BlockPos getSpawnPosition(LivingEntity entity) {
    RegistryKey<World> dimension = entity.level.dimension();
    DimensionLevelingSettings settings =
        DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
    return settings.getSpawnPosOverride().orElse(((ServerWorld) entity.level).getSharedSpawnPos());
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
    MinecraftServer server = event.getEntity().level.getServer();
    if (server == null) return;
    ResourceLocation leveledTableId =
        new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
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
    NetworkDispatcher.network_channel.send(
        PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getEntity()),
        new SyncLevelingData(entity));
  }

  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void renderEntityLevel(RenderNameplateEvent event) {
    if (ModList.get().isLoaded("neat")) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;
    LivingEntity entity = (LivingEntity) event.getEntity();
    if (!shouldShowName(entity)) return;
    event.setResult(Event.Result.ALLOW);
    Minecraft minecraft = Minecraft.getInstance();
    double distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
    if (!ForgeHooksClient.isNameplateInRenderDistance(entity, distance)) return;
    int level = getLevel(entity) + 1;
    ITextComponent entityName = event.getContent();
    ITextComponent levelString =
        new TranslationTextComponent("autoleveling.level", level).withStyle(TextFormatting.GREEN);
    float y = entity.getBbHeight() + 0.5F;
    int yShift = "deadmau5".equals(entityName.getString()) ? -10 : 0;
    event.getMatrixStack().pushPose();
    event.getMatrixStack().translate(0.0D, y, 0.0D);
    event.getMatrixStack().mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
    event.getMatrixStack().scale(-0.025F, -0.025F, 0.025F);
    Matrix4f matrix4f = event.getMatrixStack().last().pose();
    float backgroundOpacity = minecraft.options.getBackgroundOpacity(0.25F);
    int alpha = (int) (backgroundOpacity * 255.0F) << 24;
    FontRenderer font = minecraft.font;
    float x = -font.width(entityName) / 2f - 5 - font.width(levelString);
    font.drawInBatch(
        levelString,
        x,
        yShift,
        553648127,
        false,
        matrix4f,
        event.getRenderTypeBuffer(),
        !entity.isDiscrete(),
        alpha,
        event.getPackedLight());
    if (!entity.isDiscrete())
      font.drawInBatch(
          levelString,
          x,
          yShift,
          -1,
          false,
          matrix4f,
          event.getRenderTypeBuffer(),
          false,
          0,
          event.getPackedLight());
    event.getMatrixStack().popPose();
  }

  @OnlyIn(Dist.CLIENT)
  private static boolean shouldShowName(LivingEntity entity) {
    if (!hasLevel(entity)) return false;
    if (!shouldShowLevel(entity)) return false;
    Minecraft minecraft = Minecraft.getInstance();
    ClientPlayerEntity clientPlayer = minecraft.player;
    if (clientPlayer == null) return false;
    boolean alwaysShowLevel = Config.COMMON.alwaysShowLevel.get();
    boolean showLevelWhenLookingAt = Config.COMMON.showLevelWhenLookingAt.get();
    if (!alwaysShowLevel && !(showLevelWhenLookingAt && minecraft.crosshairPickEntity == entity))
      return false;
    return Minecraft.renderNames()
        && entity != minecraft.getCameraEntity()
        && !entity.isInvisibleTo(clientPlayer)
        && !entity.isVehicle()
        && clientPlayer.canSee(entity);
  }

  private static boolean shouldSetLevel(Entity entity) {
    if (entity.level.isClientSide) return false;
    if (!canHaveLevel(entity)) return false;
    return !entity.getTags().contains("autoleveling_spawned");
  }

  private static int createLevelForEntity(LivingEntity entity, double distance) {
    MinecraftServer server = entity.getServer();
    if (server == null) return 0;
    LevelingSettings levelingSettings =
        EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
    if (levelingSettings == null) {
      RegistryKey<World> dimension = entity.level.dimension();
      levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
    }
    int level = levelingSettings.startingLevel - 1;
    int maxLevel = levelingSettings.maxLevel;
    level += (int) (levelingSettings.levelsPerDistance * distance);
    level += (int) Math.pow(distance, distance * levelingSettings.levelPowerPerDistance) - 1;
    if (entity.getY() < 64) {
      double deepness = 64 - entity.getY();
      level += (int) (levelingSettings.levelsPerDeepness * deepness);
      level += (int) Math.pow(deepness, deepness * levelingSettings.levelPowerPerDeepness) - 1;
    }
    int levelBonus = levelingSettings.randomLevelBonus + 1;
    level += levelingSettings.startingLevel - 1;
    if (levelBonus > 0) level += entity.getRandom().nextInt(levelBonus);
    level = Math.abs(level);
    level += WorldLevelingData.get((ServerWorld) entity.level).getLevelBonus();
    if (maxLevel > 0) level = Math.min(level, maxLevel - 1);
    GlobalLevelingData globalLevelingData = GlobalLevelingData.get(server);
    level += globalLevelingData.getLevelBonus();
    return level;
  }

  @SubscribeEvent
  public static void applyDamageBonus(LivingHurtEvent event) {
    DamageSource damageSource = event.getSource();
    if (!(damageSource.getEntity() instanceof LivingEntity)) return;
    LivingEntity attacker = (LivingEntity) damageSource.getEntity();
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
    ModifiableAttributeInstance attributeInstance = entity.getAttribute(attribute);
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
    MinecraftServer server = entity.level.getServer();
    if (server == null) return;
    for (EquipmentSlotType slot : EquipmentSlotType.values()) {
      LootTable equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
      if (equipmentTable == LootTable.EMPTY) continue;
      LootContext lootContext = createEquipmentLootContext(entity);
      equipmentTable
          .getRandomItems(lootContext)
          .forEach(itemStack -> entity.setItemSlot(slot, itemStack));
    }
  }

  private static LootTable getEquipmentLootTableForSlot(
      MinecraftServer server, LivingEntity entity, EquipmentSlotType equipmentSlot) {
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
    return builder.create(LootParameterSets.ENTITY);
  }

  private static LootContext createEquipmentLootContext(LivingEntity entity) {
    return new LootContext.Builder((ServerWorld) entity.level)
        .withRandom(entity.getRandom())
        .withParameter(LootParameters.THIS_ENTITY, entity)
        .withParameter(LootParameters.ORIGIN, entity.position())
        .create(LootParameterSets.SELECTOR);
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
