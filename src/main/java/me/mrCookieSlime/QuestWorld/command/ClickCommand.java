package me.mrCookieSlime.QuestWorld.command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ClickCommand implements Listener {
	private static Map<UUID, Runnable> callbacks = new HashMap<>();

	public static UUID add(Runnable callback) {
		UUID key = UUID.randomUUID();
		callbacks.put(key, callback);
		return key;
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String parts[] = event.getMessage().split(" ", 3);
		if(parts.length == 2 && parts[0].equals("/qw-invoke")) {
			UUID key;
			try{
				key = UUID.fromString(parts[1]);
			}
			catch(IllegalArgumentException e) {
				return;
			}
			
			Runnable callback = callbacks.remove(key);
			if(callback != null)
				callback.run();
			
			event.setCancelled(true);
		}
	}
}
