package com.questworld.newquest.extension;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.questworld.newquest.Condition;
import com.questworld.newquest.Rule;

public class SurviveRule extends Rule implements Listener {
	public SurviveRule() {
		super(MakeRuleKey("survive"));
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
	}
	
	private class SurviveCondition extends Condition {

		public SurviveCondition() {
			super(SurviveRule.this);
		}

		@Override
		public boolean test(Event someEvent) {
			PlayerDeathEvent event = (PlayerDeathEvent) someEvent;
			
			event.getEntity().getUniqueId(); // compare with players from some other argument
			return true;
		}
		
	}
}
