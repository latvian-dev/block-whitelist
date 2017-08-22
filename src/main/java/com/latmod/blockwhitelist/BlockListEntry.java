package com.latmod.blockwhitelist;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public abstract class BlockListEntry implements Predicate<IBlockState>
{
	public ITextComponent message = null;

	public BlockListEntry setMessage(@Nullable ITextComponent m)
	{
		message = m;
		return this;
	}
}