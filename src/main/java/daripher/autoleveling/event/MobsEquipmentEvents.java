package daripher.autoleveling.event;

import java.util.stream.Stream;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsEquipmentEvents
{
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onEntityJoinWorld(EntityJoinLevelEvent event)
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
		MinecraftServer server = entity.getLevel().getServer();
		LootContext lootContext = createEquipmentLootContext(entity);
		
		Stream.of(EquipmentSlot.values()).forEach(slot ->
		{
			LootTable equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
			equipmentTable.getRandomItems(lootContext).forEach(itemStack -> entity.setItemSlot(slot, itemStack));
		});
	}
	
	private static LootTable getEquipmentLootTableForSlot(MinecraftServer server, LivingEntity entity, EquipmentSlot equipmentSlot)
	{
		ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
		return server.getLootTables().get(new ResourceLocation(entityId.getNamespace(), "equipment/" + entityId.getPath() + "_" + equipmentSlot.getName()));
	}
	
	private static LootContext createEquipmentLootContext(LivingEntity entity)
	{
		ServerLevel serverLevel = (ServerLevel) entity.level;
		Builder builder = new Builder(serverLevel).withRandom(entity.getRandom()).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position());
		return builder.create(LootContextParamSets.SELECTOR);
	}
}