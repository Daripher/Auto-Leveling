package daripher.autoleveling;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.init.AutoLevelingItems;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod {
  public static final String MOD_ID = "autoleveling";
  public static final Logger LOGGER = LogManager.getLogger(AutoLevelingMod.MOD_ID);

  public AutoLevelingMod() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    AutoLevelingLootItemConditions.REGISTRY.register(modEventBus);
    AutoLevelingItems.REGISTRY.register(modEventBus);
    AutoLevelingAttributes.REGISTRY.register(modEventBus);
  }

  @SubscribeEvent
  public static void attachMobAttributes(EntityAttributeModificationEvent event) {
    event
        .getTypes()
        .forEach(
            entityType -> {
              event.add(entityType, AutoLevelingAttributes.PROJECTILE_DAMAGE_BONUS.get());
              event.add(entityType, AutoLevelingAttributes.EXPLOSION_DAMAGE_BONUS.get());
            });
  }
}
