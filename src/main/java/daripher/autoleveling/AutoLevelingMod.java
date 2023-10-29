package daripher.autoleveling;

import com.mojang.logging.LogUtils;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.init.AutoLevelingItems;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import daripher.autoleveling.init.AutoLevelingTabs;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(AutoLevelingMod.MOD_ID)
public class AutoLevelingMod {
  public static final Logger LOGGER = LogUtils.getLogger();
  public static final String MOD_ID = "autoleveling";

  public AutoLevelingMod() {
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    AutoLevelingLootItemConditions.REGISTRY.register(modEventBus);
    AutoLevelingItems.REGISTRY.register(modEventBus);
    AutoLevelingAttributes.REGISTRY.register(modEventBus);
    AutoLevelingTabs.REGISTRY.register(modEventBus);
    Config.registerCommonConfig();
  }

  @SubscribeEvent
  public static void attachMobAttributes(EntityAttributeModificationEvent event) {
    event
        .getTypes()
        .forEach(
            entityType -> {
              event.add(entityType, AutoLevelingAttributes.PROJECTILE_DAMAGE_MULTIPLIER.get());
              event.add(entityType, AutoLevelingAttributes.EXPLOSION_DAMAGE_MULTIPLIER.get());
            });
  }
}
