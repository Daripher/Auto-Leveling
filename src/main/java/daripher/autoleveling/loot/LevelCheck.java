package daripher.autoleveling.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public record LevelCheck(int min, int max) implements LootItemCondition {
  public @NotNull LootItemConditionType getType() {
    return AutoLevelingLootItemConditions.LEVEL_CHECK.get();
  }

  public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.THIS_ENTITY);
  }

  public boolean test(LootContext context) {
    if (!context.hasParam(LootContextParams.THIS_ENTITY)) return false;
    Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
    if (!MobsLevelingEvents.hasLevel(entity)) return false;
    int level = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
    return level >= min && level <= max;
  }

  public static class Serializer
      implements net.minecraft.world.level.storage.loot.Serializer<LevelCheck> {
    public void serialize(
        JsonObject jsonObject, LevelCheck levelCheck, @NotNull JsonSerializationContext context) {
      jsonObject.addProperty("min", levelCheck.min);
      jsonObject.addProperty("max", levelCheck.max);
    }

    public @NotNull LevelCheck deserialize(
        @NotNull JsonObject jsonObject, @NotNull JsonDeserializationContext context) {
      int min = GsonHelper.getAsInt(jsonObject, "min", 0);
      int max = GsonHelper.getAsInt(jsonObject, "max", 0);
      return new LevelCheck(min, max);
    }
  }
}
