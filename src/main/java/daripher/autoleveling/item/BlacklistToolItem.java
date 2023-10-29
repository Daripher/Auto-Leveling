package daripher.autoleveling.item;

import daripher.autoleveling.config.Config;
import java.util.List;
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

public class BlacklistToolItem extends Item {
  public BlacklistToolItem() {
    super(new Properties().stacksTo(1));
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack itemStack, Player player, LivingEntity entity, InteractionHand hand) {
    if (!player.level().isClientSide) blacklistEntity(player, entity);
    return InteractionResult.SUCCESS;
  }

  protected void blacklistEntity(Player player, LivingEntity entity) {
    String id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
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
      ItemStack itemStack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
    components.add(Component.translatable(getDescriptionId() + ".tooltip"));
  }
}
