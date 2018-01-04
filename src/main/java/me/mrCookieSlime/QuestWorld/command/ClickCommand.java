package me.mrCookieSlime.QuestWorld.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ClickCommand implements Listener {
	private static Map<UUID, Runnable> callbacks = new HashMap<>();
	
	private static Map<UUID, Set<UUID>> linked_callbacks = new HashMap<>();

	public static UUID add(Runnable callback) {
		UUID key = UUID.randomUUID();
		callbacks.put(key, callback);
		return key;
	}
	
	public static void link(UUID source, UUID callbackHandle) {
		Set<UUID> linked = linked_callbacks.get(source);
		
		if(linked != null) {
			linked.add(callbackHandle);
			return;
		}
		
		linked = new HashSet<UUID>();
		linked.add(callbackHandle);
		linked_callbacks.put(source, linked);
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
			
			Set<UUID> linked = linked_callbacks.get(event.getPlayer().getUniqueId());
			if(linked != null)
				for(UUID link : linked)
					callbacks.remove(link);
			
			if(callback != null)
				callback.run();
			
			event.setCancelled(true);
		}
	}
}
