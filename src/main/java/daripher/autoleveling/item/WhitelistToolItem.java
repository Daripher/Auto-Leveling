package daripher.autoleveling.item;

import java.util.List;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.init.AutoLevelingCreativeTabs;
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

public class WhitelistToolItem extends Item {
	public WhitelistToolItem() {
		super(new Properties().tab(AutoLevelingCreativeTabs.AUTO_LEVELING).stacksTo(1));
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity entity, InteractionHand hand) {
		if (!player.level.isClientSide) {
			var entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
			var whitelistedEntities = Config.COMMON.whitelistedMobs.get();

			if (whitelistedEntities.contains(entityId)) {
				whitelistedEntities.remove(entityId);
				player.sendSystemMessage(Component.translatable(getDescriptionId() + ".removed", entityId));
			} else {
				whitelistedEntities.add(entityId);
				player.sendSystemMessage(Component.translatable(getDescriptionId() + ".added", entityId));
			}

			Config.COMMON.whitelistedMobs.set(whitelistedEntities);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
		components.add(Component.translatable(getDescriptionId() + ".tooltip"));
	}
}
