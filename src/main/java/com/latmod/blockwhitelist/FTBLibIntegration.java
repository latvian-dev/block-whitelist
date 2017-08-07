package com.latmod.blockwhitelist;

import com.feed_the_beast.ftbl.api.EventHandler;
import com.feed_the_beast.ftbl.api.events.ReloadEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
@EventHandler
public class FTBLibIntegration
{
	@SubscribeEvent
	public static void onReload(ReloadEvent event)
	{
		if (event.getSide().isServer() && !BlockWhitelist.reload())
		{
			ITextComponent c = new TextComponentString("Failed to reload BlockWhitelist!");
			c.getStyle().setColor(TextFormatting.RED);
			event.getSender().sendMessage(c);
		}
	}
}