package me.mrCookieSlime.QuestWorld.manager;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

public class StatusManager {
	private HashMap<UUID, PlayerStatus> statuses = new HashMap<>();
	
	public PlayerStatus get(OfflinePlayer player) {
		PlayerStatus result = statuses.get(player.getUniqueId());
		
		if(result == null) {
			result = new PlayerStatus(player);
			statuses.put(player.getUniqueId(), result);
		}
		
		return result;
	}
	
	public void unload(OfflinePlayer player) {
		PlayerStatus status = statuses.get(player.getUniqueId());
		
		if(status != null) {
			status.unload();
			statuses.remove(player.getUniqueId());
		}
	}
}
