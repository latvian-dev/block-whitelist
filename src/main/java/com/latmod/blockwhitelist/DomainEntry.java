package com.latmod.blockwhitelist;

import net.minecraft.block.state.IBlockState;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class DomainEntry implements Predicate<IBlockState>
{
	private final String domain;

	public DomainEntry(String d)
	{
		domain = d;
	}

	@Override
	public boolean test(IBlockState state)
	{
		return Objects.equals(domain, state.getBlock().getRegistryName().getResourceDomain());
	}

	public String toString()
	{
		return domain + ":*";
	}
}