package com.questworld.api.lang;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.questworld.api.contract.IPlayerStatus;

public class PlayerReplacements extends BaseReplacements<Player> {
	private final IPlayerStatus status;
	private final OfflinePlayer player;
	
	public PlayerReplacements(IPlayerStatus status) {
		this(status, (Player)status.getPlayer());
	}
	
	@SuppressWarnings("deprecation")
	public PlayerReplacements(String playerName) {
		this(Bukkit.getOfflinePlayer(playerName));
	}
	
	public PlayerReplacements(OfflinePlayer player) {
		this(null, player);
	}
	
	private PlayerReplacements(IPlayerStatus status, OfflinePlayer player) {
		super("player.");
		this.status = status;
		this.player = player;
	}
	
	@Override
	public Class<Player> forClass() {
		return Player.class;
	}

	@Override
	public String getReplacement(String base, String fullKey) {
		switch (base) {
			case "name":
				return player.getName();
				
			case "world":
				if (player instanceof Player) {
					return ((Player)player).getWorld().getName();
				}
				
				return "";
				
			case "uuid":
				return player.getUniqueId().toString();
				
			case "progressbar":
				
				return status != null ? status.progressString() : "";
		}
		
		return "";
	}

}
