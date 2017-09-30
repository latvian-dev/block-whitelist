package com.latmod.blockwhitelist;

import com.feed_the_beast.ftbl.lib.internal.FTBLibFinals;
import com.feed_the_beast.ftbl.lib.util.CommonUtils;
import com.feed_the_beast.ftbl.lib.util.JsonUtils;
import com.feed_the_beast.ftbl.lib.util.StringUtils;
import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.properties.IProperty;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod(modid = BlockWhitelist.MOD_ID, name = BlockWhitelist.MOD_NAME, version = BlockWhitelist.VERSION, acceptedMinecraftVersions = "[1.12,)", dependencies = "required-after:" + FTBLibFinals.MOD_ID, acceptableRemoteVersions = "*")
public class BlockWhitelist
{
	public static final String MOD_ID = "blockwhitelist";
	public static final String MOD_NAME = "Block Whitelist";
	public static final String VERSION = "@VERSION@";

	static Map<Integer, BlockList> map;
	private static File configFile;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		map = new LinkedHashMap<>();
		configFile = new File(event.getModConfigurationDirectory(), "blockwhitelist.json");
	}

	@Mod.EventHandler
	public void onPostInit(FMLPostInitializationEvent event)
	{
		reload();
	}

	static boolean reload()
	{
		map.clear();

		if (!configFile.exists())
		{
			BlockList list = new BlockList();
			list.whitelist = true;
			list.message = StringUtils.color(new TextComponentString("Example config/blockwhitelist.json loaded!"), TextFormatting.RED);
			list.entries.add(new BlockEntry(Blocks.GRASS));
			list.entries.add(new StateEntry(Blocks.STONE, Collections.singletonMap(BlockStone.VARIANT, BlockStone.EnumType.GRANITE)));
			list.entries.add(new BlockEntry(Blocks.LOG));
			list.entries.add(new DomainEntry("buildcraft").setMessage(StringUtils.color(new TextComponentString("Buildcraft is banned! Edit config/blockwhitelist.json!"), TextFormatting.RED)));
			map.put(0, list);

			list = new BlockList();
			list.whitelist = false;
			list.message = null;
			map.put(-1, list);

			try
			{
				JsonArray array = new JsonArray();

				for (Map.Entry<Integer, BlockList> entry : map.entrySet())
				{
					JsonObject object = new JsonObject();
					object.addProperty("dimension", entry.getKey());
					list = entry.getValue();
					object.add("message", JsonUtils.serializeTextComponent(list.message));

					JsonArray alist = new JsonArray();

					for (BlockListEntry entry1 : list.entries)
					{
						if (entry1.message == null)
						{
							alist.add(entry1.toString());
						}
						else
						{
							JsonObject o = new JsonObject();
							o.add("message", JsonUtils.serializeTextComponent(entry1.message));
							o.addProperty("value", entry1.toString());
							alist.add(o);
						}
					}

					object.add(list.whitelist ? "whitelist" : "blacklist", alist);
					array.add(object);
				}

				JsonUtils.toJson(configFile, array);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		try
		{
			JsonArray array = JsonUtils.fromJson(configFile).getAsJsonArray();

			for (JsonElement element : array)
			{
				JsonObject object = element.getAsJsonObject();
				BlockList list = new BlockList();
				list.whitelist = object.has("whitelist");

				for (JsonElement element1 : object.get(list.whitelist ? "whitelist" : "blacklist").getAsJsonArray())
				{
					String s;
					ITextComponent msg = null;
					BlockListEntry entry = null;

					if (element1.isJsonObject())
					{
						JsonObject o = element1.getAsJsonObject();
						s = o.get("value").getAsString();

						if (o.has("message"))
						{
							msg = JsonUtils.deserializeTextComponent(o.get("message"));
						}
					}
					else
					{
						s = element1.getAsString();
					}

					if (s.isEmpty() || s.equals("*"))
					{
						entry = EverythingEntry.INSTANCE;
					}
					else if (s.endsWith(":*"))
					{
						entry = new DomainEntry(s.substring(0, s.lastIndexOf(':')));
					}
					else if (s.charAt(s.length() - 1) == ']')
					{
						int idx = s.indexOf('[');
						Block block = Block.REGISTRY.getObject(new ResourceLocation(s.substring(0, idx)));

						if (block != Blocks.AIR)
						{
							String p = s.substring(idx + 1, s.length() - 1);

							if (p.equals("*"))
							{
								entry = new BlockEntry(block);
							}
							else
							{
								Map<IProperty<?>, Comparable<?>> properties = new HashMap<>();

								if (!p.isEmpty())
								{
									for (String entry1 : s.split(","))
									{
										String[] entry1s = entry1.split("=");
										IProperty<?> property = block.getBlockState().getProperty(entry1s[0]);

										if (property != null)
										{
											Optional<?> optional = property.parseValue(entry1s[1]);

											if (optional.isPresent())
											{
												properties.put(property, CommonUtils.cast(optional.get()));
											}
										}
									}
								}

								entry = properties.isEmpty() ? new BlockEntry(block) : new StateEntry(block, properties);
							}
						}
					}
					else
					{
						Block block = Block.REGISTRY.getObject(new ResourceLocation(s));

						if (block != Blocks.AIR)
						{
							entry = new BlockEntry(block);
						}
					}

					if (entry != null)
					{
						list.entries.add(entry.setMessage(msg));
					}
				}

				if (object.has("message"))
				{
					list.message = JsonUtils.deserializeTextComponent(object.get("message"));
				}

				map.put(object.get("dimension").getAsInt(), list);
			}

			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
}