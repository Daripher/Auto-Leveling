package daripher.autoleveling.item;

import java.util.List;

import daripher.autoleveling.config.Config;
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
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity entity, InteractionHand hand) {
		if (player.level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		var entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
		var blacklistedEntities = Config.COMMON.blacklistedMobs.get();
		if (blacklistedEntities.contains(entityId)) {
			blacklistedEntities.remove(entityId);
			player.sendSystemMessage(Component.translatable(getDescriptionId() + ".removed", entityId));
		} else {
			blacklistedEntities.add(entityId);
			player.sendSystemMessage(Component.translatable(getDescriptionId() + ".added", entityId));
		}
		Config.COMMON.blacklistedMobs.set(blacklistedEntities);
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
		components.add(Component.translatable(getDescriptionId() + ".tooltip"));
	}
}
