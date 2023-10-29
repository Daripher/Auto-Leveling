package daripher.autoleveling.item;

import daripher.autoleveling.config.Config;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
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
    super(new Properties().stacksTo(1));
  }

  @Override
  public @NotNull InteractionResult interactLivingEntity(
      @NotNull ItemStack itemStack,
      Player player,
      @NotNull LivingEntity entity,
      @NotNull InteractionHand hand) {
    if (!player.level().isClientSide) blacklistEntity(player, entity);
    return InteractionResult.SUCCESS;
  }

  protected void blacklistEntity(Player player, LivingEntity entity) {
    String id =
        Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())).toString();
    List<String> blacklist = Config.COMMON.blacklistedMobs.get();
    if (blacklist.contains(id)) {
      blacklist.remove(id);
      player.sendSystemMessage(Component.translatable(getDescriptionId() + ".removed", id));
    } else {
      blacklist.add(id);
      player.sendSystemMessage(Component.translatable(getDescriptionId() + ".added", id));
    }
    Config.COMMON.blacklistedMobs.set(blacklist);
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack itemStack,
      Level level,
      List<Component> components,
      @NotNull TooltipFlag tooltipFlag) {
    components.add(Component.translatable(getDescriptionId() + ".tooltip"));
  }
}
