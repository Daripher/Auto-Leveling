package daripher.autoleveling.data;

import java.util.function.BiConsumer;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeModifierContainer implements AttributeModifierPoolEntry
{
	private final AttributeModifier modifier;
	private final Attribute attribute;
	private final int weight;
	private final int quality;
	
	public AttributeModifierContainer(int weight, int quality, Attribute attribute, AttributeModifier modifier)
	{
		this.weight = weight;
		this.quality = quality;
		this.attribute = attribute;
		this.modifier = modifier;
	}
	
	@Override
	public int getWeight(float luck)
	{
		return Math.max(Mth.floor(weight + quality * luck), 0);
	}
	
	@Override
	public void createModifier(BiConsumer<AttributeModifier, Attribute> consumer)
	{
		consumer.accept(modifier, attribute);
	}
}
