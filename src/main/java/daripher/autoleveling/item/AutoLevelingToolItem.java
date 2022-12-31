package daripher.autoleveling.item;

import daripher.autoleveling.init.AutoLevelingCreativeTabs;
import net.minecraft.item.Item;

public class AutoLevelingToolItem extends Item {
	public AutoLevelingToolItem() {
		super(new Properties().tab(AutoLevelingCreativeTabs.AUTO_LEVELING).stacksTo(1));
	}
}
