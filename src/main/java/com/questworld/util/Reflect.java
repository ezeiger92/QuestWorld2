package com.questworld.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class Reflect {
	private Reflect() {
	}

	private static final Class<?> serverClass = Bukkit.getServer().getClass();
	private static final String CBS = serverClass.getName().replaceFirst("[^.]+$", "");
	private static final String NMS;

	private static final MultiAdapter adapter;

	static {
		String nms = null;

		try {
			nms = serverClass.getMethod("getServer").getReturnType().getName().replaceFirst("[^.]+$", "");
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
		}
		
		String version = nms.substring(22, nms.length() - 1);
		
		if(isClass("net.techcable.tacospigot.TacoSpigotConfig")) {
			version += "_TACO";
		}
		else if(isClass("com.destroystokyo.paper.PaperConfig")) {
			version += "_PAPER";
		}
		else if(isClass("org.spigotmc.SpigotConfig")) {
			version += "_SPIGOT";
		}

		adapter = new MultiAdapter(version);

		NMS = nms;
	}
	
	public static boolean isClass(String className) {
		try {
			Class.forName(className);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public static Class<?> NMS(String className) throws ClassNotFoundException {
		return Class.forName(NMS + className);
	}
	
	public static Class<?> CBS(String className) throws ClassNotFoundException {
		return Class.forName(CBS + className);
	}

	public static void addAdapter(VersionAdapter child) {
		adapter.addAdapter(child);
	}

	public static VersionAdapter getAdapter() {
		return adapter;
	}

	public static void playerAddChannel(Player p, String s) throws Exception {
		CBS("entity.CraftPlayer").getMethod("addChannel", String.class).invoke(p, s);
	}

	public static void playerRemoveChannel(Player p, String s) throws Exception {
		CBS("entity.CraftPlayer").getMethod("removeChannel", String.class).invoke(p, s);
	}

	public static ItemStack nmsPickBlock(Block block) throws Exception {
		World w = block.getWorld();
		Chunk c = block.getChunk();

		Object world = w.getClass().getMethod("getHandle").invoke(w);
		Object chunk = c.getClass().getMethod("getHandle").invoke(c);

		Object blockposition = NMS("BlockPosition").getConstructor(int.class, int.class, int.class)
				.newInstance(block.getX(), block.getY(), block.getZ());
		Object iblockdata = chunk.getClass().getMethod("getBlockData", blockposition.getClass()).invoke(chunk,
				blockposition);

		Class<?> worldClass = NMS("World");
		@SuppressWarnings("deprecation")
		Object rawblock = Bukkit.getUnsafe().getClass().getMethod("getBlock", Block.class).invoke(null, block);
		Class<?> iblockclass = NMS("IBlockData");
		Object rawitemstack = rawblock.getClass().getMethod("a", worldClass, blockposition.getClass(), iblockclass)
				.invoke(rawblock, world, blockposition, iblockdata);

		return (ItemStack) CBS("inventory.CraftItemStack")
				.getMethod("asCraftMirror", rawitemstack.getClass()).invoke(null, rawitemstack);
	}

	public static String nmsGetItemName(ItemStack stack) throws Exception {
		if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
			return stack.getItemMeta().getDisplayName();
		Object rstack = CBS("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class)
				.invoke(null, stack);
		return (String) rstack.getClass().getMethod("getName").invoke(rstack);
	}
}
