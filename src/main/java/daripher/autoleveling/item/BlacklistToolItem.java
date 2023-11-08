package daripher.autoleveling.item;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingCreativeTabs;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class BlacklistToolItem extends Item {
  public BlacklistToolItem() {
    super(new Properties().tab(AutoLevelingCreativeTabs.AUTO_LEVELING).stacksTo(1));
  }

  @Nonnull
  @Override
  public ActionResultType interactLivingEntity(
      @Nonnull ItemStack itemStack,
      PlayerEntity player,
      @Nonnull LivingEntity entity,
      @Nonnull Hand hand) {
    if (player.level.isClientSide) {
      return ActionResultType.SUCCESS;
    }
    String entityId =
        Objects.requireNonNull(ForgeRegistries.ENTITIES.getKey(entity.getType())).toString();
    List<String> blacklistedEntities = Config.COMMON.blacklistedMobs.get();

    if (blacklistedEntities.contains(entityId)) {
      blacklistedEntities.remove(entityId);
      player.sendMessage(
          new TranslationTextComponent(getDescriptionId() + ".removed", entityId), Util.NIL_UUID);
    } else {
      blacklistedEntities.add(entityId);
      player.sendMessage(
          new TranslationTextComponent(getDescriptionId() + ".added", entityId), Util.NIL_UUID);
    }

    Config.COMMON.blacklistedMobs.set(blacklistedEntities);
    return ActionResultType.SUCCESS;
  }

  @Override
  public void appendHoverText(
      @Nonnull ItemStack itemStack,
      World level,
      List<ITextComponent> components,
      @Nonnull ITooltipFlag tooltipFlag) {
    components.add(new TranslationTextComponent(getDescriptionId() + ".tooltip"));
  }
}
