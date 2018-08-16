package com.questworld.api.context;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerContext extends Context {	
	public PlayerContext(OfflinePlayer player) {
		this(player, "player");
	}
	
	public PlayerContext(OfflinePlayer player, String prefix) {
		super(prefix);

		map(null, player::getName);
		map("name", player::getName);
		map("uuid", player::getUniqueId);
		
		if(player instanceof Player) {
			Player online = (Player) player;
			
			map("health", online::getHealth);
		}
	}
}
