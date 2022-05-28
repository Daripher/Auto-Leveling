package daripher.autoleveling.data;

import java.util.function.BiConsumer;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface AttributeModifierPoolEntry
{
	int getWeight(float luck);
	
	void createModifier(BiConsumer<AttributeModifier, Attribute> consumer);
}
