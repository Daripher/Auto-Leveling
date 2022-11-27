package daripher.autoleveling.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.data.EntitiesLevelingSettingsReloader;
import daripher.autoleveling.data.LevelingSettings;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents
{
	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (!LevelingDataProvider.canHaveLevel(event.getEntity()))
			return;
		
		if (event.getEntity().level.isClientSide)
			return;
		
		if (event.getEntity().getTags().contains("autoleveling_spawn"))
			return;
		
		LivingEntity entity = (LivingEntity) event.getEntity();
		ServerWorld level = ((ServerWorld) entity.level);
		BlockPos spawnPos = level.getSharedSpawnPos();
		double distance = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
		int monsterLevel = getLevelForEntity(entity, distance);
		entity.addTag("autoleveling_spawn");
		LevelingDataProvider.get(entity).ifPresent(levelingData -> levelingData.setLevel(monsterLevel));
		LevelingDataProvider.applyAttributeBonuses(entity, monsterLevel);
		LevelingDataProvider.addEquipment(entity);
	}
	
	@SubscribeEvent
	public static void onLivingExperienceDrop(LivingExperienceDropEvent event)
	{
		if (LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			LevelingDataProvider.get(event.getEntityLiving()).ifPresent(levelingData ->
			{
				int level = levelingData.getLevel() + 1;
				int exp = event.getOriginalExperience();
				double expBonus = Config.COMMON.expBonus.get() * level;
				event.setDroppedExperience((int) (exp + exp * expBonus));
			});
		}
	}
	
	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event)
	{
		if (LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			ResourceLocation resourcelocation = new ResourceLocation(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs");
			LootTable loottable = event.getEntity().level.getServer().getLootTables().get(resourcelocation);
			int lastHurtByPlayerTime = ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, event.getEntityLiving(), "field_70718_bc");
			Method createLootContext = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_213363_a", boolean.class, DamageSource.class);
			LootContext.Builder lootcontext$builder;
			
			try
			{
				lootcontext$builder = (LootContext.Builder) createLootContext.invoke(event.getEntity(), lastHurtByPlayerTime > 0, event.getSource());
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				e.printStackTrace();
				return;
			}
			
			LootContext ctx = lootcontext$builder.create(LootParameterSets.ENTITY);
			loottable.getRandomItems(ctx).forEach(event.getEntity()::spawnAtLocation);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onRenderNameplate(RenderNameplateEvent event)
	{
		if (!Config.COMMON.showLevel.get())
		{
			return;
		}
		
		if (LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			Minecraft minecraft = Minecraft.getInstance();
			LivingEntity entity = (LivingEntity) event.getEntity();
			
			if (shouldShowName(entity))
			{
				event.setResult(Event.Result.ALLOW);
				double distance = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
				
				if (ForgeHooksClient.isNameplateInRenderDistance(entity, distance))
				{
					LevelingDataProvider.get(entity).ifPresent(levelingData ->
					{
						int level = levelingData.getLevel() + 1;
						ITextComponent entityName = event.getContent();
						ITextComponent levelString = new StringTextComponent("" + level).withStyle(TextFormatting.GREEN);
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
						float x = -font.width(entityName) / 2 - 5 - font.width(levelString);
						font.drawInBatch(levelString, x, yShift, 553648127, false, matrix4f, event.getRenderTypeBuffer(), !entity.isDiscrete(), alpha, event.getPackedLight());
						
						if (!entity.isDiscrete())
						{
							font.drawInBatch(levelString, x, yShift, -1, false, matrix4f, event.getRenderTypeBuffer(), false, 0, event.getPackedLight());
						}
						
						event.getMatrixStack().popPose();
					});
				}
			}
		}
	}
	
	private static int getLevelForEntity(LivingEntity entity, double distanceFromSpawn)
	{
		LevelingSettings levelingSettings = EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
		
		if (levelingSettings == null)
		{
			RegistryKey<World> dimension = entity.level.dimension();
			levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
		}
		
		int monsterLevel = (int) (levelingSettings.levelsPerDistance * distanceFromSpawn);
		int maxLevel = levelingSettings.maxLevel;
		int levelBonus = levelingSettings.randomLevelBonus + 1;
		monsterLevel += levelingSettings.startingLevel - 1;
		
		if (levelBonus > 0)
		{
			monsterLevel += entity.getRandom().nextInt(levelBonus);
		}
		
		monsterLevel = Math.abs(monsterLevel);
		
		if (maxLevel > 0)
		{
			monsterLevel = Math.min(monsterLevel, maxLevel - 1);
		}
		
		MinecraftServer server = entity.getServer();
		GlobalLevelingData data = GlobalLevelingData.get(server);
		monsterLevel += data.getLevelBonus();
		return monsterLevel;
	}
	
	@OnlyIn(Dist.CLIENT)
	protected static boolean shouldShowName(LivingEntity entity)
	{
		Minecraft minecraft = Minecraft.getInstance();
		return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && !entity.isInvisibleTo(minecraft.player) && !entity.isVehicle() && minecraft.player.canSee(entity);
	}
}
