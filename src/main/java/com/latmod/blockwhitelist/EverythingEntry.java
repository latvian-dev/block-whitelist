package com.latmod.blockwhitelist;

import net.minecraft.block.state.IBlockState;

/**
 * @author LatvianModder
 */
public class EverythingEntry extends BlockListEntry
{
	public static final EverythingEntry INSTANCE = new EverythingEntry();

	private EverythingEntry()
	{
	}

	@Override
	public boolean test(IBlockState state)
	{
		return true;
	}

	public String toString()
	{
		return "*";
	}
}