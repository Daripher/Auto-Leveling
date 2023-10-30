package daripher.autoleveling.saveddata;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class WorldLevelingData extends SavedData {
  private float levelBonus;
  public int tickCount;

  private static WorldLevelingData create() {
    return new WorldLevelingData();
  }

  @SubscribeEvent
  public static void tick(TickEvent.LevelTickEvent event) {
    if (event.phase != TickEvent.Phase.START) return;
    if (event.level.isClientSide) return;
    WorldLevelingData levelingData = WorldLevelingData.get((ServerLevel) event.level);
    levelingData.tick(event.level);
  }

  private static WorldLevelingData load(CompoundTag tag) {
    WorldLevelingData data = WorldLevelingData.create();
    data.levelBonus = tag.getFloat("LevelBonus");
    data.tickCount = tag.getInt("TickCount");
    return data;
  }

  public static WorldLevelingData get(ServerLevel level) {
    return level
        .getDataStorage()
        .computeIfAbsent(WorldLevelingData::load, WorldLevelingData::create, "world_leveling");
  }

  private void tick(Level world) {
    tickCount++;
    // 24_000 ticks is one Minecraft day
    if (tickCount >= 24_000) {
      levelBonus +=
          DimensionsLevelingSettingsReloader.getSettingsForDimension(world.dimension())
              .levelsPerDay();
      tickCount -= 24_000;
    }
    setDirty();
  }

  @Override
  public @NotNull CompoundTag save(CompoundTag tag) {
    tag.putFloat("LevelBonus", levelBonus);
    tag.putInt("TickCount", tickCount);
    return tag;
  }

  public int getLevelBonus() {
    return (int) levelBonus;
  }
}
