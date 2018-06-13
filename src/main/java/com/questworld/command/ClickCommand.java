package com.questworld.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import com.questworld.util.AutoListener;

public final class ClickCommand extends AutoListener {
	private static Map<UUID, Runnable> callbacks = new HashMap<>();

	private static Map<UUID, Set<UUID>> linked_callbacks = new HashMap<>();

	public ClickCommand(Plugin plugin) {
		register(plugin);
	}

	public static UUID add(Runnable callback) {
		UUID key = UUID.randomUUID();
		callbacks.put(key, callback);
		return key;
	}

	public static UUID add(UUID source, Runnable callback) {
		UUID callbackHandle = add(callback);
		Set<UUID> linked = linked_callbacks.get(source);

		if (linked != null)
			linked.add(callbackHandle);
		else {
			linked = new HashSet<UUID>();
			linked.add(callbackHandle);
			linked_callbacks.put(source, linked);
		}
		return callbackHandle;
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String parts[] = event.getMessage().split(" ", 3);
		if (parts.length == 2 && parts[0].equals("/qw-invoke")) {
			UUID key;
			try {
				key = UUID.fromString(parts[1]);
			}
			catch (IllegalArgumentException e) {
				return;
			}

			Runnable callback = callbacks.remove(key);

			if (callback != null) {
				Set<UUID> linked = linked_callbacks.remove(event.getPlayer().getUniqueId());
				if (linked != null)
					for (UUID link : linked)
						callbacks.remove(link);

				callback.run();
			}

			event.setCancelled(true);
		}
	}
}
