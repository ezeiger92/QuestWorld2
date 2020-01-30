package com.questworld.newquest;

import org.bukkit.event.Event;

import com.questworld.util.UniqueKey;

public abstract class Rule {
	private static final String NAMESPACE = "questworld-rule";
	private final UniqueKey ID;
	
	// All live instances of rules, should not be stored here
	// Only for mental model
	private static final RuleConfigDB database = new RuleConfigDB();
	
	protected void testConditions(Event someEvent) {
		for(NodeConfig config : database.getConfigs(getClass())) {
			// Need to get association of rule&config&player
			test(someEvent, config, new Profile());
		}
	}
	
	public abstract boolean test(Event someEvent, NodeConfig config, Profile profile);
	
	protected void registerConfigs(NodeConfig... configs) {
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
