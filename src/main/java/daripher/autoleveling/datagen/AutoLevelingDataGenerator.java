package daripher.autoleveling.datagen;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, bus = Bus.MOD)
public class AutoLevelingDataGenerator {
  @SubscribeEvent
  public static void onGatherData(GatherDataEvent event) {
    DataGenerator dataGenerator = event.getGenerator();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    PackOutput packOutput = dataGenerator.getPackOutput();
    dataGenerator.addProvider(event.includeClient(), new AutoLevelingLanguageProvider(packOutput));
    dataGenerator.addProvider(
        event.includeClient(), new AutoLevelingItemModelProvider(packOutput, existingFileHelper));
  }
}
