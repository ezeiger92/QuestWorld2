package me.mrCookieSlime.QuestWorld.manager;

import java.util.HashMap;
import java.util.UUID;

public class StatusManager {
	private HashMap<UUID, PlayerStatus> statuses = new HashMap<>();
	
	public PlayerStatus get(UUID uuid) {
		PlayerStatus result = statuses.get(uuid);
		
		if(result == null) {
			result = new PlayerStatus(uuid);
			statuses.put(uuid, result);
		}
		
		return result;
	}
	
	public void unload(UUID uuid) {
		PlayerStatus status = statuses.get(uuid);
		
		if(status != null) {
			status.unload();
			statuses.remove(uuid);
		}
	}
	
	public void unloadAll() {
		statuses.clear();
	}
}
