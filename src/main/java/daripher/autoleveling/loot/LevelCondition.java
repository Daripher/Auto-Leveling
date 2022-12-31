package daripher.autoleveling.loot;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import daripher.autoleveling.api.ILevelingData;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.init.AutoLevelingLootConditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;

public class LevelCondition implements ILootCondition {
	final int min;
	final int max;

	public LevelCondition(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public LootConditionType getType() {
		return AutoLevelingLootConditions.LEVEL_CHECK;
	}

	@Override
	public Set<LootParameter<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootParameters.THIS_ENTITY);
	}

	@Override
	public boolean test(LootContext context) {
		if (!context.hasParam(LootParameters.THIS_ENTITY)) {
			return false;
		}

		Entity entity = context.getParamOrNull(LootParameters.THIS_ENTITY);

		if (!LevelingDataProvider.canHaveLevel(entity)) {
			return false;
		}

		if (!(entity instanceof LivingEntity)) {
			return false;
		}

		ILevelingData levelingData = LevelingDataProvider.getLevelingData((LivingEntity) entity).orElse(null);

		if (levelingData == null) {
			return false;
		}

		int level = levelingData.getLevel() + 1;
		return level >= min && level <= max;
	}

	public static IBuilder correctLevel(int min, int max) {
		return () -> {
			return new LevelCondition(min, max);
		};
	}

	public static class Serializer implements ILootSerializer<LevelCondition> {
		public void serialize(JsonObject jsonObject, LevelCondition levelCheck, JsonSerializationContext context) {
			jsonObject.addProperty("min", levelCheck.min);
			jsonObject.addProperty("max", levelCheck.max);
		}

		public LevelCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
			int min = JSONUtils.getAsInt(jsonObject, "min", 0);
			int max = JSONUtils.getAsInt(jsonObject, "max", 0);
			return new LevelCondition(min, max);
		}
	}
}
