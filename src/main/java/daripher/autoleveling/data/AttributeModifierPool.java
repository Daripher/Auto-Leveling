package daripher.autoleveling.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeModifierPool
{
	private static final Map<ResourceLocation, AttributeModifierPool> POOLS_BY_ID = new HashMap<>();
	private final AttributeModifierContainer[] entries;
	
	public AttributeModifierPool(ResourceLocation tableId, JsonObject jsonObject)
	{
		JsonArray jsonEntriesArray = jsonObject.get("entries").getAsJsonArray();
		entries = new AttributeModifierContainer[jsonEntriesArray.size()];
		
		for (int i = 0; i < jsonEntriesArray.size(); i++)
		{
			JsonObject jsonEntryObject = jsonEntriesArray.get(i).getAsJsonObject();
			int weight = jsonEntryObject.get("weight").getAsInt();
			int quality = jsonEntryObject.get("quality").getAsInt();
			String attributeId = jsonEntryObject.get("attribute").getAsString();
			Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeId));
			JsonObject jsonModifierObject = jsonEntryObject.get("modifier").getAsJsonObject();
			double value = jsonModifierObject.get("value").getAsDouble();
			int operation = jsonModifierObject.get("operation").getAsInt();
			AttributeModifier modifier = new AttributeModifier("leveled_item", value, AttributeModifier.Operation.fromValue(operation));
			entries[i] = new AttributeModifierContainer(weight, quality, attribute, modifier);
		}
		
		POOLS_BY_ID.put(tableId, this);
	}
	
	public void addRandomModifiers(BiConsumer<AttributeModifier, Attribute> consumer, LootContext lootContext, int amount)
	{
		Random random = lootContext.getRandom();
		List<AttributeModifierContainer> list = new ArrayList<>();
		
		for (AttributeModifierContainer entry : entries)
		{
			list.add(entry);
		}
		
		MutableInt overallWeight = new MutableInt();
		int listSize = list.size();
		
		for (AttributeModifierContainer lootpoolentry : list)
		{
			overallWeight.add(lootpoolentry.getWeight(lootContext.getLuck()));
		}
		
		if (overallWeight.intValue() != 0 && listSize != 0)
		{
			if (listSize <= amount)
			{
				for (AttributeModifierContainer lootpoolentry : list)
				{
					lootpoolentry.createModifier(consumer);
				}
			}
			else
			{
				for (int i = 0; i < amount; amount--)
				{
					int j = random.nextInt(overallWeight.intValue());
					
					for (AttributeModifierContainer lootpoolentry : list)
					{
						j -= lootpoolentry.getWeight(lootContext.getLuck());
						
						if (j < 0)
						{
							lootpoolentry.createModifier(consumer);
							
							if (amount == 1)
							{
								return;
							}
							else
							{
								overallWeight.subtract(lootpoolentry.getWeight(lootContext.getLuck()));
								list.remove(lootpoolentry);
								break;
							}
						}
					}
				}
			}
		}
	}
	
	public static AttributeModifierPool get(ResourceLocation id)
	{
		return POOLS_BY_ID.get(id);
	}
}
