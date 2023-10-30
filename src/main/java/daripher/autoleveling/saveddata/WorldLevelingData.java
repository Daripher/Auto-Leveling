package daripher.autoleveling.saveddata;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class WorldLevelingData extends WorldSavedData {
  private float levelBonus;
  public int tickCount;

  public WorldLevelingData() {
    super("world_leveling");
  }

  private static WorldLevelingData create() {
    return new WorldLevelingData();
  }

  @SubscribeEvent
  public static void tick(TickEvent.WorldTickEvent event) {
    if (event.phase != TickEvent.Phase.START) return;
    if (event.world.isClientSide) return;
    WorldLevelingData levelingData = WorldLevelingData.get((ServerWorld) event.world);
    levelingData.tick(event.world);
  }

  public void load(CompoundNBT tag) {
    levelBonus = tag.getFloat("LevelBonus");
    tickCount = tag.getInt("TickCount");
  }

  public static WorldLevelingData get(ServerWorld level) {
    return level.getDataStorage().computeIfAbsent(WorldLevelingData::create, "world_leveling");
  }

  private void tick(World world) {
    tickCount++;
    // 24_000 ticks is one Minecraft day
    if (tickCount >= 24_000) {
      levelBonus +=
          DimensionsLevelingSettingsReloader.getSettingsForDimension(world.dimension())
              .getLevelsPerDay();
      tickCount -= 24_000;
    }
    setDirty();
  }

  @Override
  public @Nonnull CompoundNBT save(CompoundNBT tag) {
    tag.putFloat("LevelBonus", levelBonus);
    tag.putInt("TickCount", tickCount);
    return tag;
  }

  public int getLevelBonus() {
    return (int) levelBonus;
  }
}
