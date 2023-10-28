package daripher.autoleveling.item;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingCreativeTabs;
import java.util.List;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class BlacklistToolItem extends Item {
  public BlacklistToolItem() {
    super(new Properties().tab(AutoLevelingCreativeTabs.AUTO_LEVELING).stacksTo(1));
  }

  @Override
  public @NotNull InteractionResult interactLivingEntity(
      @NotNull ItemStack itemStack,
      Player player,
      @NotNull LivingEntity entity,
      @NotNull InteractionHand hand) {
    if (player.level.isClientSide) return InteractionResult.SUCCESS;
    String entityId =
        Objects.requireNonNull(ForgeRegistries.ENTITIES.getKey(entity.getType())).toString();
    List<String> blacklistedEntities = Config.COMMON.blacklistedMobs.get();
    if (blacklistedEntities.contains(entityId)) {
      blacklistedEntities.remove(entityId);
      player.sendMessage(
          new TranslatableComponent(getDescriptionId() + ".removed", entityId), Util.NIL_UUID);
    } else {
      blacklistedEntities.add(entityId);
      player.sendMessage(
          new TranslatableComponent(getDescriptionId() + ".added", entityId), Util.NIL_UUID);
    }
    Config.COMMON.blacklistedMobs.set(blacklistedEntities);
    return InteractionResult.SUCCESS;
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack itemStack,
      Level level,
      List<Component> components,
      @NotNull TooltipFlag tooltipFlag) {
    components.add(new TranslatableComponent(getDescriptionId() + ".tooltip"));
  }
}
