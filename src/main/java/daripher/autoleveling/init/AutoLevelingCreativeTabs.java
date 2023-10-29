package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AutoLevelingCreativeTabs {
  public static final CreativeModeTab AUTO_LEVELING =
      new CreativeModeTab(AutoLevelingMod.MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() {
          return new ItemStack(AutoLevelingItems.BLACKLIST_TOOL.get());
        }
      };
}
