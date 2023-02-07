package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.item.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AutoLevelingItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, AutoLevelingMod.MOD_ID);

	public static final RegistryObject<Item> BLACKLIST_TOOL = REGISTRY.register("blacklist_tool", BlacklistToolItem::new);
	public static final RegistryObject<Item> WHITELIST_TOOL = REGISTRY.register("whitelist_tool", WhitelistToolItem::new);
}
