package com.latmod.blockwhitelist;

import com.feed_the_beast.ftbl.lib.util.CommonUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Map;

/**
 * @author LatvianModder
 */
public class StateEntry extends BlockListEntry
{
	private final Map<IProperty<?>, Comparable<?>> properties;
	private IBlockState state;

	public StateEntry(Block block, Map<IProperty<?>, Comparable<?>> p)
	{
		properties = p;
		state = block.getDefaultState();

		for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet())
		{
			state = state.withProperty(entry.getKey(), CommonUtils.cast(entry.getValue()));
		}
	}

	@Override
	public boolean test(IBlockState s)
	{
		if (state == s)
		{
			return true;
		}
		else if (state.getBlock() != s.getBlock())
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
		if (state == Blocks.AIR.getDefaultState())
		{
			return "minecraft:air";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(Block.REGISTRY.getNameForObject(state.getBlock()));

		if (!properties.isEmpty())
		{
			builder.append('[');
			boolean first = true;

			for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					builder.append(',');
				}

				builder.append(entry.getKey().getName());
				builder.append('=');
				builder.append(entry.getKey().getName(CommonUtils.cast(entry.getValue())));
			}

			builder.append(']');
		}

		return builder.toString();
	}
}