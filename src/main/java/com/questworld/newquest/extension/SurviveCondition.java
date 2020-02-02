package com.questworld.newquest.extension;

import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.questworld.newquest.NodeConfig;
import com.questworld.newquest.Profile;
import com.questworld.newquest.Condition;

/**
 * Prototype for how rules will work
 * Nothing is final
 * Nothing is sacred
 * Not even the concept of 'Rules'
 * 
 * @author Erik
 *
 */
public class SurviveCondition extends Condition implements Listener {
	public SurviveCondition() {
		super(ConditionKey("survive"));
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		testConditions(event);
	}

	@Override
	public boolean test(Event someEvent, NodeConfig<Condition.Properties> config, Profile profile) {
		PlayerDeathEvent event = (PlayerDeathEvent) someEvent;
		Properties props = config.deserialize(Properties.class);
		
		if (event.getKeepInventory() && props.keep_inv_counted) {
			return true;
		}
		
		if(props.ignored_worlds.contains(event.getEntity().getWorld().getName())) {
			return true;
		}
		
		if(event.getEntity().getUniqueId().equals(profile.getUniqueId())) {
			return false;
		}
		
		return true;
	}
	
	private static class Properties extends Condition.Properties {
		public boolean keep_inv_counted = true;
		public List<String> ignored_worlds = Arrays.asList("minigames");
	}
}
