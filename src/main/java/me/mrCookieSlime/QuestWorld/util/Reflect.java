package me.mrCookieSlime.QuestWorld.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public final class Reflect {
	private static final Class<?> serverClass = Bukkit.getServer().getClass();
	private static final String CBS = serverClass.getName().replaceFirst("[^.]+$", "");
	private static final String NMS;
	
	static {
		String nms = null;
		try {
			nms = serverClass.getMethod("getServer").getReturnType().getName().replaceFirst("[^.]+$", "");
		}
		catch(RuntimeException e) {
			throw e;
		}
		catch(Exception e) {
		}
		
		NMS = nms;
	}
	
	public static ItemStack nmsPickBlock(Block block) throws Exception {
		World w = block.getWorld();
		Chunk c = block.getChunk();
		
		Object world = w.getClass().getMethod("getHandle").invoke(w);
		Object chunk = c.getClass().getMethod("getHandle").invoke(c);
		
		Object blockposition = Class.forName(NMS + "BlockPosition")
				.getConstructor(int.class, int.class, int.class)
				.newInstance(block.getX(), block.getY(), block.getZ());
		Object iblockdata = chunk.getClass().getMethod("getBlockData", blockposition.getClass()).invoke(chunk, blockposition);
		
		Class<?> worldClass = Class.forName(NMS + "World");
		@SuppressWarnings("deprecation")
		Object rawblock = Bukkit.getUnsafe().getClass().getMethod("getBlock", Block.class).invoke(null, block);
		Class<?> iblockclass = Class.forName(NMS + "IBlockData");
		Object rawitemstack = rawblock.getClass().getMethod("a", worldClass, blockposition.getClass(), iblockclass)
			.invoke(rawblock, world, blockposition, iblockdata);

		return (ItemStack)Class.forName(CBS + "inventory.CraftItemStack").getMethod("asCraftMirror", rawitemstack.getClass()).invoke(null, rawitemstack);
	}
	
	public static String nmsGetItemName(ItemStack stack) throws Exception {
		if(stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
			return stack.getItemMeta().getDisplayName();
		Object rstack = Class.forName(Reflect.CBS + "inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);
		return (String)rstack.getClass().getMethod("getName").invoke(rstack);
	}
}
