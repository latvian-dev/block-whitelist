package com.latmod.blockwhitelist;

import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class BlockList
{
	public boolean whitelist = true;
	public final List<BlockListEntry> entries = new ArrayList<>();
	public ITextComponent message = null;
}