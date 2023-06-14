package daripher.autoleveling.loot.condition;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record LevelCheck(int min, int max) implements LootItemCondition {
	public LootItemConditionType getType() {
		return AutoLevelingLootItemConditions.LEVEL_CHECK.get();
	}

	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.THIS_ENTITY);
	}

	public boolean test(LootContext context) {
		if (!context.hasParam(LootContextParams.THIS_ENTITY)) return false;
		var entity = context.getParam(LootContextParams.THIS_ENTITY);
		if (!MobsLevelingEvents.hasLevel(entity)) return false;
		var level = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
		return level >= min && level <= max;
	}

	public static LootItemCondition.Builder correctLevel(int min, int max) {
		return () -> new LevelCheck(min, max);
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LevelCheck> {
		public void serialize(JsonObject jsonObject, LevelCheck levelCheck, JsonSerializationContext context) {
			jsonObject.addProperty("min", levelCheck.min);
			jsonObject.addProperty("max", levelCheck.max);
		}

		public LevelCheck deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
			var min = GsonHelper.getAsInt(jsonObject, "min", 0);
			var max = GsonHelper.getAsInt(jsonObject, "max", 0);
			return new LevelCheck(min, max);
		}
	}
}
