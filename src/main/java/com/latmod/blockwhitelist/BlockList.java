package com.latmod.blockwhitelist;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class BlockList
{
	public boolean whitelist = true;
	public final List<Predicate<IBlockState>> predicates = new ArrayList<>();
	public ITextComponent message = null;

	public boolean contains(IBlockState state)
	{
		for (Predicate<IBlockState> p : predicates)
		{
			if (p.test(state))
			{
				return true;
			}
		}

		return false;
	}
}