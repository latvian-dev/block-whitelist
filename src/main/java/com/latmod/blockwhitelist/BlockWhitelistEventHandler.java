package com.latmod.blockwhitelist;

import com.feed_the_beast.ftbl.api.EventHandler;
import com.feed_the_beast.ftbl.api.events.ReloadEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
@EventHandler
public class BlockWhitelistEventHandler
{
	public static final ResourceLocation RELOAD_CONFIG = new ResourceLocation(BlockWhitelist.MOD_ID, "config");

	@SubscribeEvent
	public static void registerReloadIds(ReloadEvent.RegisterIds event)
	{
		event.register(RELOAD_CONFIG);
	}

	@SubscribeEvent
	public static void onReload(ReloadEvent event)
	{
		if (event.getSide().isServer() && event.reload(RELOAD_CONFIG) && !BlockWhitelist.reload())
		{
			event.failedToReload(RELOAD_CONFIG);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onBlockPlace(BlockEvent.PlaceEvent event)
	{
		if (!event.getWorld().isRemote)
		{
			BlockList list = BlockWhitelist.map.get(event.getWorld().provider.getDimension());
			IBlockState state = event.getPlacedBlock();

			if (list != null)
			{
				for (BlockListEntry entry : list.entries)
				{
					if (entry.test(state) != list.whitelist)
					{
						event.setCanceled(true);

						if (entry.message != null)
						{
							event.getPlayer().sendStatusMessage(entry.message, true);
						}
						else if (list.message != null)
						{
							event.getPlayer().sendStatusMessage(list.message, true);
						}

						return;
					}
				}
			}
		}
	}
}