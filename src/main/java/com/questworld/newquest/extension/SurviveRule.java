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
		testConditions(event);
	}

	@Override
	public boolean test(Event someEvent, Condition condition) {
		PlayerDeathEvent event = (PlayerDeathEvent) someEvent;
		SurviveCondition cond = (SurviveCondition) condition;
		
		if (event.getKeepInventory() && cond.ignoredIfKeepInv()) {
			return true;
		}
		
		event.getEntity().getUniqueId(); // compare with players from some other argument
		//return true;
		
		return false;
	}
	
	public class SurviveCondition extends Condition {
		public SurviveCondition() {
			super(SurviveRule.this);
		}
		
		boolean ignoredIfKeepInv() {
			return getProperty("keepinv-not-counted", o -> Boolean.parseBoolean(o.toString()));
		}
	}
}
