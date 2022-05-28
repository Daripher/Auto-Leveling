package daripher.autoleveling.event;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.capability.LevelingDataProvider;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.data.AttributeModifierPool;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class LootLevelingEvents
{
	@SubscribeEvent
	public static void onEntityJoinWorld(LivingDeathEvent event)
	{
		LivingEntity entity = (LivingEntity) event.getEntity();
		
		if (!Config.COMMON.lootDropEnabled.get())
		{
			return;
		}
		
		if (entity.level.isClientSide)
		{
			return;
		}
		
		if (!(entity instanceof Enemy) && !(entity instanceof NeutralMob))
		{
			return;
		}
		
		LevelingDataProvider.get(entity).ifPresent(levelingData ->
		{
			int monsterLevel = levelingData.getLevel();
			double lootChance = monsterLevel * Config.COMMON.lootDropChancePerLevel.get() + Config.COMMON.lootDropChance.get();
			
			if (!(entity.getLastHurtByMob() instanceof Player))
			{
				return;
			}
			
			for (int i = 0; i < lootChance - lootChance % 1 + 1; lootChance--)
			{
				if (entity.getRandom().nextDouble() >= lootChance)
				{
					return;
				}
				
				Player killer = (Player) entity.getLastHurtByMob();
				float playerLuck = killer.getLuck();
				ResourceLocation lootTableId = new ResourceLocation(AutoLevelingMod.MOD_ID, "leveling_equipment");
				LootTable lootTable = entity.level.getServer().getLootTables().get(lootTableId);
				LootContext lootContext = createLootContext(entity, event.getSource(), killer);
				lootTable.getRandomItems(lootContext).forEach(itemStack ->
				{
					int modifiersAmount = getRandomModifiersAmount(entity, playerLuck);
					
					if (modifiersAmount > 0)
					{
						if (itemStack.getItem() instanceof SwordItem)
						{
							ResourceLocation modifiersPoolId = new ResourceLocation(AutoLevelingMod.MOD_ID, "sword_modifiers");
							addItemModifiers(itemStack, modifiersPoolId, lootContext, modifiersAmount, monsterLevel, EquipmentSlot.MAINHAND);
						}
						else if (itemStack.getItem() instanceof AxeItem)
						{
							ResourceLocation modifiersPoolId = new ResourceLocation(AutoLevelingMod.MOD_ID, "axe_modifiers");
							addItemModifiers(itemStack, modifiersPoolId, lootContext, modifiersAmount, monsterLevel, EquipmentSlot.MAINHAND);
						}
						else if (itemStack.getItem() instanceof ArmorItem)
						{
							ArmorItem armorItem = (ArmorItem) itemStack.getItem();
							
							if (armorItem.getSlot() == EquipmentSlot.HEAD)
							{
								ResourceLocation modifiersPoolId = new ResourceLocation(AutoLevelingMod.MOD_ID, "helmet_modifiers");
								addItemModifiers(itemStack, modifiersPoolId, lootContext, modifiersAmount, monsterLevel, EquipmentSlot.HEAD);
							}
							else if (armorItem.getSlot() == EquipmentSlot.CHEST)
							{
								ResourceLocation modifiersPoolId = new ResourceLocation(AutoLevelingMod.MOD_ID, "chestplate_modifiers");
								addItemModifiers(itemStack, modifiersPoolId, lootContext, modifiersAmount, monsterLevel, EquipmentSlot.CHEST);
							}
							else if (armorItem.getSlot() == EquipmentSlot.LEGS)
							{
								ResourceLocation modifiersPoolId = new ResourceLocation(AutoLevelingMod.MOD_ID, "leggings_modifiers");
								addItemModifiers(itemStack, modifiersPoolId, lootContext, modifiersAmount, monsterLevel, EquipmentSlot.LEGS);
							}
							else if (armorItem.getSlot() == EquipmentSlot.FEET)
							{
								ResourceLocation modifiersPoolId = new ResourceLocation(AutoLevelingMod.MOD_ID, "boots_modifiers");
								addItemModifiers(itemStack, modifiersPoolId, lootContext, modifiersAmount, monsterLevel, EquipmentSlot.FEET);
							}
						}
					}
					
					itemStack.getTag().putInt("itemlevel", monsterLevel);
					itemStack.getTag().putInt("modifiers", modifiersAmount);
					entity.spawnAtLocation(itemStack);
				});
			}
		});
	}
	
	@SubscribeEvent
	public static void onRenderTooltip(RenderTooltipEvent.GatherComponents event)
	{
		if (!Config.COMMON.showItemLevel.get() && !Config.COMMON.showItemRarity.get())
		{
			return;
		}
		
		if (event.getItemStack().hasTag() && event.getItemStack().getTag().contains("itemlevel"))
		{
			event.getTooltipElements().remove(0);
			Component itemTitleComponent;
			
			if (!event.getItemStack().hasCustomHoverName())
			{
				String itemTitle = event.getItemStack().getItem().getName(event.getItemStack()).getString();
				
				if (Config.COMMON.showItemLevel.get())
				{
					itemTitle += " " + event.getItemStack().getTag().getInt("itemlevel");
				}
				
				itemTitleComponent = new TextComponent(itemTitle);
			}
			else
			{
				itemTitleComponent = event.getItemStack().getHoverName();
			}
			
			if (Config.COMMON.showItemRarity.get())
			{
				ChatFormatting rarityStyle = ChatFormatting.WHITE;
				
				switch (event.getItemStack().getTag().getInt("modifiers"))
				{
					case 1:
						rarityStyle = ChatFormatting.GREEN;
						break;
					case 2:
						rarityStyle = ChatFormatting.YELLOW;
						break;
					case 3:
						rarityStyle = ChatFormatting.LIGHT_PURPLE;
						break;
					case 4:
						rarityStyle = ChatFormatting.GOLD;
						break;
				}
				
				itemTitleComponent = new TextComponent(itemTitleComponent.getString()).withStyle(rarityStyle);
			}
			
			event.getTooltipElements().add(0, Either.<FormattedText, TooltipComponent>left(itemTitleComponent));
		}
	}
	
	private static void addItemModifiers(ItemStack itemStack, ResourceLocation modifiersPoolId, LootContext lootContext, int modifiersAmount, int monsterLevel, EquipmentSlot slot)
	{
		Multimap<Attribute, AttributeModifier> modifiers = itemStack.getItem().getAttributeModifiers(slot, itemStack);
		AttributeModifierPool modifiersPool = AttributeModifierPool.get(modifiersPoolId);
		List<AttributeModifier> ignoredModifiers = new ArrayList<>();
		
		modifiersPool.addRandomModifiers((modifier, attribute) ->
		{
			AttributeModifier adjustedModifier = new AttributeModifier(modifier.getName(), modifier.getAmount() * monsterLevel, modifier.getOperation());
			
			for (AttributeModifier modifier_ : modifiers.get(attribute))
			{
				if (modifier_.getOperation() == adjustedModifier.getOperation())
				{
					adjustedModifier = new AttributeModifier(modifier.getName(), modifier.getAmount() * monsterLevel + modifier_.getAmount(), modifier.getOperation());
					ignoredModifiers.add(modifier_);
				}
			}
			
			itemStack.addAttributeModifier(attribute, adjustedModifier, slot);
		}, lootContext, modifiersAmount);
		
		modifiers.forEach((attribute, modifier) ->
		{
			if (!ignoredModifiers.contains(modifier))
			{
				itemStack.addAttributeModifier(attribute, modifier, slot);
			}
		});
	}
	
	private static int getRandomModifiersAmount(LivingEntity monster, float playerLuck)
	{
		double rand = monster.getRandom().nextDouble();
		
		if (rand < Config.COMMON.legendaryDropChance.get() + Config.COMMON.legendaryDropChancePerLuck.get() * playerLuck)
		{
			return 4;
		}
		else if (rand < Config.COMMON.epicDropChance.get() + Config.COMMON.epicDropChancePerLuck.get() * playerLuck)
		{
			return 3;
		}
		else if (rand < Config.COMMON.rareDropChance.get() + Config.COMMON.rareDropChancePerLuck.get() * playerLuck)
		{
			return 2;
		}
		else if (rand < Config.COMMON.uncommonDropChance.get() + Config.COMMON.uncommonDropChancePerLuck.get() * playerLuck)
		{
			return 1;
		}
		
		return 0;
	}
	
	private static LootContext createLootContext(LivingEntity entity, DamageSource damageSource, Player killer)
	{
		LootContext.Builder lootContextBuilder = new LootContext.Builder((ServerLevel) entity.level).withRandom(entity.getRandom()).withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(LootContextParams.ORIGIN, entity.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
				.withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity())
				.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer).withLuck(killer.getLuck());
		return lootContextBuilder.create(LootContextParamSets.ENTITY);
	}
}
