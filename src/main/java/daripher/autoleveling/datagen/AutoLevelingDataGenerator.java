package daripher.autoleveling.datagen;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, bus = Bus.MOD)
public class AutoLevelingDataGenerator {
  @SubscribeEvent
  public static void onGatherData(GatherDataEvent event) {
    var dataGenerator = event.getGenerator();
    var existingFileHelper = event.getExistingFileHelper();

    if (event.includeClient()) {
      dataGenerator.addProvider(new AutoLevelingLanguageProvider(dataGenerator));
      dataGenerator.addProvider(
          new AutoLevelingItemModelProvider(dataGenerator, existingFileHelper));
    }
  }
}
