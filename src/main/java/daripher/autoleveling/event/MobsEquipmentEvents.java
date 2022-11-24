package daripher.autoleveling.event;

import java.util.stream.Stream;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootContext.Builder;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsEquipmentEvents
{
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.getEntity().level.isClientSide)
		{
			return;
		}
		
		if (!LevelingDataProvider.canHaveLevel(event.getEntity()))
		{
			return;
		}
		
		LivingEntity entity = (LivingEntity) event.getEntity();
		
		if (entity.getTags().contains("autoleveling_spawn"))
		{
			return;
		}
		
		entity.addTag("autoleveling_spawn");
		MinecraftServer server = entity.getServer();
		LootContext lootContext = createEquipmentLootContext(entity);
		
		Stream.of(EquipmentSlotType.values()).forEach(slot ->
		{
			LootTable equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
			equipmentTable.getRandomItems(lootContext).forEach(itemStack -> entity.setItemSlot(slot, itemStack));
		});
	}
	
	private static LootTable getEquipmentLootTableForSlot(MinecraftServer server, LivingEntity entity, EquipmentSlotType equipmentSlot)
	{
		String entityId = ForgeRegistries.ENTITIES.getKey(entity.getType()).getPath();
		return server.getLootTables().get(new ResourceLocation(AutoLevelingMod.MOD_ID, "equipment/" + entityId + "_" + equipmentSlot.getName()));
	}
	
	private static LootContext createEquipmentLootContext(LivingEntity entity)
	{
		ServerWorld serverLevel = (ServerWorld) entity.level;
		Builder builder = new Builder(serverLevel).withRandom(entity.getRandom()).withParameter(LootParameters.THIS_ENTITY, entity).withParameter(LootParameters.ORIGIN, entity.position());
		return builder.create(LootParameterSets.SELECTOR);
	}
}
