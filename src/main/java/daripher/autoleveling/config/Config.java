package daripher.autoleveling.config;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.client.LevelPlatePos;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
  public static final Config.Common COMMON;
  public static final ForgeConfigSpec COMMON_SPEC;
  public static final Config.Client CLIENT;
  public static final ForgeConfigSpec CLIENT_SPEC;
  private static final Map<Attribute, AttributeModifier> ATTRIBUTE_BONUSES = new HashMap<>();

  static {
    Pair<Config.Common, ForgeConfigSpec> commonSpec =
        new ForgeConfigSpec.Builder().configure(Config.Common::new);
    COMMON_SPEC = commonSpec.getRight();
    COMMON = commonSpec.getLeft();
    Pair<Config.Client, ForgeConfigSpec> clientSpec =
        new ForgeConfigSpec.Builder().configure(Config.Client::new);
    CLIENT_SPEC = clientSpec.getRight();
    CLIENT = clientSpec.getLeft();
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

  public static Map<Attribute, AttributeModifier> getAttributeBonuses() {
    if (ATTRIBUTE_BONUSES.isEmpty()) {
      for (List<Object> objects : Config.COMMON.attributesBonuses.get()) {
        readAttributeBonus(objects);
      }
    }
    return ATTRIBUTE_BONUSES;
  }

  private static void readAttributeBonus(List<Object> attributeBonusConfig) {
    ResourceLocation attributeId = new ResourceLocation((String) attributeBonusConfig.get(0));
    Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
    float attributeBonus = ((Double) attributeBonusConfig.get(1)).floatValue();
    if (attribute == null) {
      AutoLevelingMod.LOGGER.error("Attribute '" + attributeId + "' can not be found!");
      return;
    }
    UUID uuid = UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
    AttributeModifier.Operation operation = AttributeModifier.Operation.MULTIPLY_BASE;
    AttributeModifier modifier =
        new AttributeModifier(uuid, "AutoLeveling", attributeBonus, operation);
    ATTRIBUTE_BONUSES.put(attribute, modifier);
  }

  public static void register() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
  }

  public static class Common {
    public final ConfigValue<List<? extends List<Object>>> attributesBonuses;
    public final ConfigValue<List<String>> blacklistedMobs;
    public final ConfigValue<List<String>> whitelistedMobs;
    public final ConfigValue<List<String>> blacklistedShownLevels;
    public final ConfigValue<Integer> startingLevel;
    public final ConfigValue<Integer> maxLevel;
    public final ConfigValue<Integer> randomLevelBonus;
    public final ConfigValue<Double> expBonus;
    public final ConfigValue<Double> levelsPerDistance;
    public final ConfigValue<Double> levelsPerDeepness;
    public final ConfigValue<Double> levelsPerDay;
    public final ConfigValue<Double> levelPowerPerDistance;
    public final ConfigValue<Double> levelPowerPerDeepness;
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
      startingLevel = builder.define("Starting level", 1);
      builder.comment("If this is equal to 0, there will be no maximum level");
      maxLevel = builder.define("Maximum level", 0);
      levelsPerDistance = builder.define("Level increase per one block distance from spawn", 0.01D);
      levelsPerDeepness =
          builder.define("Level increase per one block deepness below sea level", 0.0D);
      builder.comment(
          "If this is higher than 0, the level of monsters will be randomly increased by value between 0 and this value");
      randomLevelBonus = builder.define("Random level bonus", 0);
      builder.comment(
          "If this is higher than 0, mobs level will increase every day by specified amount");
      levelsPerDay = builder.define("Level bonus per day", 0d);
      builder.comment("Exponential level increase with distance");
      levelPowerPerDistance = builder.define("level_power_per_distance", 0d);
      builder.comment("Exponential level increase with deepness");
      levelPowerPerDeepness = builder.define("level_power_per_deepness", 0d);
      builder.pop();
    }
  }

  public static class Client {
    private static int level_text_color = -1;
    public final ConfigValue<String> levelTextColor;
    public final ConfigValue<LevelPlatePos> levelTextPosition;
    public final ConfigValue<Integer> levelTextShiftX;
    public final ConfigValue<Integer> levelTextShiftY;

    public Client(ForgeConfigSpec.Builder builder) {
      builder.push("Visuals");
      levelTextColor = builder.define("Level text color", "#1cff27", Config.Client::isColorString);
      levelTextPosition = builder.defineEnum("Level text position", LevelPlatePos.LEFT);
      levelTextShiftX = builder.define("Level text shift x", 0);
      levelTextShiftY = builder.define("Level text shift y", 0);
      builder.pop();
    }

    private static boolean isColorString(Object object) {
      if (!(object instanceof String string)) return false;
      if (!string.startsWith("#")) return false;
      string = string.substring(1);
      try {
        Integer.parseInt(string, 16);
        return true;
      } catch (NumberFormatException exception) {
        return false;
      }
    }

    public static int getLevelTextColor() {
      if (level_text_color == -1) {
        String color = CLIENT.levelTextColor.get().substring(1);
        level_text_color = Integer.parseInt(color, 16);
      }
      return level_text_color;
    }
  }
}
