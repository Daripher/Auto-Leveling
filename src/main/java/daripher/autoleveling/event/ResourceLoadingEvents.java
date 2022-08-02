package daripher.autoleveling.event;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.data.LevelingSettingsReloader;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class ResourceLoadingEvents
{
	@SubscribeEvent
	public static void onAddReloadListener(AddReloadListenerEvent event)
	{
		event.addListener(new LevelingSettingsReloader());
	}
}
