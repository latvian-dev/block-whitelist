package com.latmod.blockwhitelist;

import net.minecraft.block.state.IBlockState;

import java.util.Objects;

/**
 * @author LatvianModder
 */
public class DomainEntry extends BlockListEntry
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