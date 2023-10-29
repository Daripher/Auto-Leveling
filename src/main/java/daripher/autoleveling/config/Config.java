package daripher.autoleveling.config;

import daripher.autoleveling.AutoLevelingMod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
  public static final Common COMMON;
  public static final ForgeConfigSpec COMMON_SPEC;
  private static final Map<Attribute, Float> ATTRIBUTE_BONUSES = new HashMap<>();

  static {
    Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
    COMMON_SPEC = specPair.getRight();
    COMMON = specPair.getLeft();
  }

  private static List<List<Object>> getDefaultAttributeBonuses() {
    List<List<Object>> attributeBonuses = new ArrayList<>();
    attributeBonuses.add(Arrays.asList("minecraft:generic.movement_speed", 0.001));
    attributeBonuses.add(Arrays.asList("minecraft:generic.flying_speed", 0.001));
    attributeBonuses.add(Arrays.asList("minecraft:generic.attack_damage", 0.1));
    attributeBonuses.add(Arrays.asList("minecraft:generic.armor", 0.1));
    attributeBonuses.add(Arrays.asList("minecraft:generic.max_health", 0.1));
    attributeBonuses.add(Arrays.asList("autoleveling:monster.projectile_damage_bonus", 0.1));
    attributeBonuses.add(Arrays.asList("autoleveling:monster.explosion_damage_bonus", 0.1));
    return attributeBonuses;
  }

  private static <T> boolean isValidAttributeBonus(T object) {
    if (object instanceof List<?> list) {
      return list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Double;
    }
    return false;
  }

  public static Map<Attribute, Float> getAttributeBonuses() {
    if (ATTRIBUTE_BONUSES.isEmpty()) {
      Config.COMMON
          .attributesBonuses
          .get()
          .forEach(
              attributeBonusConfig -> {
                ResourceLocation attributeId =
                    new ResourceLocation((String) attributeBonusConfig.get(0));
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
                float attributeBonus = ((Double) attributeBonusConfig.get(1)).floatValue();
                if (attribute == null)
                  AutoLevelingMod.LOGGER.error("Attribute '" + attributeId + "' can not be found!");
                else ATTRIBUTE_BONUSES.put(attribute, attributeBonus);
              });
    }
    return ATTRIBUTE_BONUSES;
  }

  public static void registerCommonConfig() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
  }

  public static class Common {
    public final ConfigValue<List<? extends List<Object>>> attributesBonuses;
    public final ConfigValue<List<String>> blacklistedMobs;
    public final ConfigValue<List<String>> whitelistedMobs;
    public final ConfigValue<List<String>> blacklistedShownLevels;
    public final ConfigValue<Integer> defaultStartingLevel;
    public final ConfigValue<Integer> defaultMaxLevel;
    public final ConfigValue<Integer> defaultRandomLevelBonus;
    public final ConfigValue<Double> expBonus;
    public final ConfigValue<Double> defaultLevelsPerDistance;
    public final ConfigValue<Double> defaultLevelsPerDeepness;
    public final ConfigValue<Boolean> alwaysShowLevel;
    public final ConfigValue<Boolean> showLevelWhenLookingAt;

    public Common(ForgeConfigSpec.Builder builder) {
      builder.push("Mobs");
      builder.comment("Example: [\"minecraft:zombie\", \"minecraft:skeleton\"]");
      blacklistedMobs =
          builder.define("List of mobs that shouldn't be able to level up", new ArrayList<>());
      builder.comment("If this list is not empty only these mobs will be able to level up");
      builder.comment("Example: [\"minecraft:zombie\", \"minecraft:skeleton\"]");
      whitelistedMobs =
          builder.define("List of mobs that should be able to level up", new ArrayList<>());
      alwaysShowLevel = builder.define("Always show mobs levels", false);
      showLevelWhenLookingAt = builder.define("Only show levels when you look at the mob", true);
      expBonus = builder.define("Bonus experience per level", 0.1D);
      blacklistedShownLevels =
          builder.define(
              "List of mobs that should have their levels always hidden", new ArrayList<>());
      builder.pop();
      builder.push("Attributes");
      builder.comment("Contains a list of pairs of attributes and their bonuses per one level");
      attributesBonuses =
          builder.defineList(
              "Attributes bonuses per one level",
              Config::getDefaultAttributeBonuses,
              Config::isValidAttributeBonus);
      builder.pop();
      builder.push("Default levelling settings");
      defaultStartingLevel = builder.define("Starting level", 1);
      builder.comment("If this is equal to 0, there will be no maximum level");
      defaultMaxLevel = builder.define("Maximum level", 0);
      defaultLevelsPerDistance =
          builder.define("Level increase per one block distance from spawn", 0.01D);
      defaultLevelsPerDeepness =
          builder.define("Level increase per one block deepness below sea level", 0.0D);
      builder.comment(
          "If this is higher than 0, the level of monsters will be randomly increased by value between 0 and this value");
      defaultRandomLevelBonus = builder.define("Random level bonus", 0);
      builder.pop();
    }
  }
}
