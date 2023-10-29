package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AutoLevelingTabs {
  public static final DeferredRegister<CreativeModeTab> REGISTRY =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AutoLevelingMod.MOD_ID);

  public static final RegistryObject<CreativeModeTab> TOOLS =
      REGISTRY.register(
          "tools",
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup.autoleveling"))
                  .icon(() -> new ItemStack(AutoLevelingItems.BLACKLIST_TOOL.get()))
                  .displayItems(
                      (params, output) -> {
                        output.accept(AutoLevelingItems.BLACKLIST_TOOL.get());
                        output.accept(AutoLevelingItems.WHITELIST_TOOL.get());
                      })
                  .build());
}
