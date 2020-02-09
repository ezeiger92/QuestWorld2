package com.questworld.newquest.extension;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.questworld.newquest.Condition;
import com.questworld.newquest.NodeConfig;
import com.questworld.newquest.Profile;

public class WorldCondition extends Condition {
	public WorldCondition() {
		super(ConditionKey("in-world"));
	}

	@Override
	public boolean test(Event someEvent, NodeConfig<BaseProperties> config, Profile profile) {
		Properties props = config.deserialize(Properties.class);
		Player player = Bukkit.getPlayer(profile.getUniqueId());
		
		// third result: ingored
		if(player == null) {
			return false;
		}
		
		return player.getWorld().getName().equalsIgnoreCase(props.worldName);
	}
	
	private static class Properties extends BaseProperties {
		public String worldName = "world";
	}
}
