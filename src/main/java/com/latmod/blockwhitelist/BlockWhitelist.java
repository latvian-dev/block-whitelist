package com.latmod.blockwhitelist;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

@Mod(modid = BlockWhitelist.MOD_ID, name = "Block Whitelist", version = "@VERSION@", acceptedMinecraftVersions = "[1.10,)", acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber
public class BlockWhitelist
{
	public static final String MOD_ID = "blockwhitelist";

	private static Map<Integer, BlockList> map;
	private static File configFile;
	private static Gson gson;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		map = new LinkedHashMap<>();
		configFile = new File(event.getModConfigurationDirectory(), "blockwhitelist.json");
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.disableHtmlEscaping();
		builder.setLenient();
		builder.registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer());
		builder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
		builder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
		gson = builder.create();
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
			list.message = new TextComponentString("Example config/blockwhitelist.json loaded!");
			list.predicates.add(new BlockEntry(Blocks.GRASS));
			list.predicates.add(new StateEntry(Blocks.STONE, Collections.singletonMap(BlockStone.VARIANT, BlockStone.EnumType.GRANITE)));
			list.predicates.add(new BlockEntry(Blocks.LOG));
			list.predicates.add(new DomainEntry("buildcraft"));
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
					object.add("message", gson.toJsonTree(list.message));

					JsonArray alist = new JsonArray();

					for (Predicate<IBlockState> predicate : list.predicates)
					{
						alist.add(new JsonPrimitive(predicate.toString()));
					}

					object.add(list.whitelist ? "whitelist" : "blacklist", alist);
					array.add(object);
				}

				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8");
				gson.toJson(array, writer);
				writer.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		try
		{
			InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), "UTF-8");
			JsonArray array = new JsonParser().parse(reader).getAsJsonArray();
			reader.close();

			for (JsonElement element : array)
			{
				JsonObject object = element.getAsJsonObject();
				BlockList list = new BlockList();
				list.whitelist = object.has("whitelist");

				for (JsonElement element1 : object.get(list.whitelist ? "whitelist" : "blacklist").getAsJsonArray())
				{
					String[] s = element1.getAsString().split(":", 3);

					if (s.length == 1 || s.length >= 2 && s[1].equals("*"))
					{
						list.predicates.add(new DomainEntry(s[0]));
					}
					else if (s.length >= 2)
					{
						Block block = Block.REGISTRY.getObject(new ResourceLocation(s[0], s[1]));

						if (block != Blocks.AIR)
						{
							if (s.length == 2 || s[2].equals("*"))
							{
								list.predicates.add(new BlockEntry(block));
							}
							else if (s[2].startsWith("{") && s[2].endsWith("}"))
							{
								Map<IProperty<?>, Comparable<?>> properties = new HashMap<>();

								for (Map.Entry<String, JsonElement> entry : gson.fromJson(s[2], JsonObject.class).entrySet())
								{
									IProperty<?> property = block.getBlockState().getProperty(entry.getKey());

									if (property != null)
									{
										Optional<?> optional = property.parseValue(entry.getValue().getAsString());

										if (optional.isPresent())
										{
											properties.put(property, cast(optional.get()));
										}
									}
								}

								list.predicates.add(new StateEntry(block, properties));
							}
						}
					}
				}

				if (object.has("message"))
				{
					JsonElement message = object.get("message");
					if (message.isJsonPrimitive())
					{
						list.message = new TextComponentString(message.getAsString());
						list.message.getStyle().setColor(TextFormatting.RED);
					}
					else
					{
						list.message = gson.fromJson(message, ITextComponent.class);
					}
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

	public static <T> T cast(Object o)
	{
		return (T) o;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onBlockPlace(BlockEvent.PlaceEvent event)
	{
		if (!event.getWorld().isRemote)
		{
			BlockList list = map.get(event.getWorld().provider.getDimension());
			if (list != null && list.contains(event.getPlacedBlock()) != list.whitelist)
			{
				event.setCanceled(true);

				if (list.message != null)
				{
					event.getPlayer().sendStatusMessage(list.message, true);
				}
			}
		}
	}
}