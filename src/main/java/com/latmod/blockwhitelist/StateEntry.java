package com.latmod.blockwhitelist;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class StateEntry implements Predicate<IBlockState>
{
	private final Map<IProperty<?>, Comparable<?>> properties;
	private IBlockState state;

	public StateEntry(Block block, Map<IProperty<?>, Comparable<?>> p)
	{
		properties = p;
		state = block.getDefaultState();

		for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet())
		{
			state = state.withProperty(entry.getKey(), BlockWhitelist.cast(entry.getValue()));
		}
	}

	@Override
	public boolean test(IBlockState s)
	{
		if (state == s)
		{
			return true;
		}

		if (state.getBlock() != s.getBlock())
		{
			return false;
		}

		for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet())
		{
			if (!entry.getValue().equals(s.getProperties().get(entry.getKey())))
			{
				return false;
			}
		}

		return true;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder(state.getBlock().getRegistryName() + ":{");

		boolean first = true;

		for (Map.Entry<IProperty<?>, Comparable<?>> property : properties.entrySet())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				builder.append(',');
			}

			builder.append(property.getKey().getName());
			builder.append('=');
			builder.append(property.getKey().getName(BlockWhitelist.cast(property.getValue())));
		}

		return builder.append('}').toString();
	}
}