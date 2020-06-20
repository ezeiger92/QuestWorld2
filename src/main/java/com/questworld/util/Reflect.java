package com.questworld.util;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.questworld.util.adapter.MultiAdapter;
import com.questworld.util.adapter.TypedAdapter;
import com.questworld.util.adapter.VersionAdapter;

public final class Reflect {
	private Reflect() {
	}

	private static final String CBS;
	private static final String NMS;

	private static final MultiAdapter adapter;
	private static final Version version;

	static {
		Version current = Version.current();
		CBS = "org.bukkit.craftbukkit." + current + ".";
		NMS = "net.minecraft.server." + current + ".";
		String serialVersion = current.toString();
		
		if(isClass("net.techcable.tacospigot.TacoSpigotConfig")) {
			serialVersion += "_TACO";
		}
		else if(isClass("com.destroystokyo.paper.PaperConfig")) {
			serialVersion += "_PAPER";
		}
		else if(isClass("org.spigotmc.SpigotConfig")) {
			serialVersion += "_SPIGOT";
		}

		version = Version.ofString(serialVersion);
		adapter = new MultiAdapter();
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

	public static TypedAdapter getAdapter() {
		return adapter;
	}
	
	public static Version specificVersion() {
		return version;
	}

	public static void playerAddChannel(Player p, String s) throws Exception {
		CBS("entity.CraftPlayer").getMethod("addChannel", String.class).invoke(p, s);
	}

	public static void playerRemoveChannel(Player p, String s) throws Exception {
		CBS("entity.CraftPlayer").getMethod("removeChannel", String.class).invoke(p, s);
	}

	public static void serverAddChannel(Plugin plugin, String channel) throws Exception {
		Object messenger = plugin.getServer().getMessenger();
		Method m = messenger.getClass().getDeclaredMethod("addToOutgoing", Plugin.class, String.class);
		boolean access = m.isAccessible();
		
		m.setAccessible(true);
		m.invoke(messenger, plugin, channel);
		m.setAccessible(access);
	}
	
	public static void serverRemoveChannel(Plugin plugin, String channel) throws Exception {
		Object messenger = plugin.getServer().getMessenger();
		Method m = messenger.getClass().getDeclaredMethod("removeFromOutgoing", Plugin.class, String.class);
		boolean access = m.isAccessible();
		
		m.setAccessible(true);
		m.invoke(messenger, plugin, channel);
		m.setAccessible(access);
	}

	public static String nmsGetItemName(ItemStack stack) throws Exception {
		if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
			return stack.getItemMeta().getDisplayName();
		Object rstack = CBS("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class)
				.invoke(null, stack);

		Object chatMessage = rstack.getClass().getMethod("getName").invoke(rstack);
		
		return (String) chatMessage.getClass().getMethod("getText").invoke(chatMessage);
	}
}
