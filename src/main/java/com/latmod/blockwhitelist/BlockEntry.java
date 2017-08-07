package com.latmod.blockwhitelist;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class BlockEntry implements Predicate<IBlockState>
{
	private final Block block;

	public BlockEntry(Block b)
	{
		block = b;
	}

	@Override
	public boolean test(IBlockState state)
	{
		return state.getBlock() == block;
	}

	public String toString()
	{
		return String.valueOf(block.getRegistryName());
	}
}