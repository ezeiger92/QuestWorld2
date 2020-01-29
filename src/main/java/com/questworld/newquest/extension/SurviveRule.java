package com.questworld.newquest.extension;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.questworld.newquest.RuleConfig;
import com.questworld.newquest.Rule;

public class SurviveRule extends Rule implements Listener {
	public SurviveRule() {
		super(MakeRuleKey("survive"));
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		testConditions(event);
	}

	@Override
	public boolean test(Event someEvent, RuleConfig config, Player player) {
		PlayerDeathEvent event = (PlayerDeathEvent) someEvent;
		Properties props = deserialize(config);
		
		if (event.getKeepInventory() && props.keepInvCounted) {
			return true;
		}
		
		if(player == event.getEntity()) {
			return false;
		}
		
		return true;
	}
	
	protected Properties deserialize(RuleConfig properties) {
		Properties props = new Properties();
		
		props.keepInvCounted = properties.getProperty("keepinv-not-counted", o -> Boolean.parseBoolean(o.toString()));
		
		return props;
	}
	
	private static class Properties {
		public boolean keepInvCounted = true;
	}
}
