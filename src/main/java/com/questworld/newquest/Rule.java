package com.questworld.newquest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.questworld.util.UniqueKey;

public abstract class Rule {
	private static final String NAMESPACE = "questworld-rule";
	private final UniqueKey ID;
	
	// All live instances of rules, should not be stored here
	// Only for mental model
	private static final RuleConfigDB database = new RuleConfigDB();
	
	protected void testConditions(Event someEvent) {
		for(RuleConfig config : database.getConfigs(getClass())) {
			// Need to get association of rule&config&player
			test(someEvent, config, null);
		}
	}
	
	public abstract boolean test(Event someEvent, RuleConfig config, Player player);
	
	protected void registerConfigs(RuleConfig... configs) {
		database.storeConfigs(getClass(), configs);
	}
	
	protected Rule(UniqueKey ID) {
		this.ID = ID;
	}
	
	protected static UniqueKey MakeRuleKey(String key) {
		return new UniqueKey(NAMESPACE, key);
	}
	
	public UniqueKey getID() {
		return ID;
	}
	
	@Override
	public String toString() {
		return ID.toString();
	}
	
	public enum Result {
		ALLOWED,
		DENIED,
		OBJECTIVE_FAILURE,
		QUSET_SUCCESS,
		QUEST_FAILURE,
	}
}
