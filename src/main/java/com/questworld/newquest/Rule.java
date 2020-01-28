package com.questworld.newquest;

import org.bukkit.event.Event;

import com.questworld.util.UniqueKey;

public abstract class Rule {
	private static final String NAMESPACE = "questworld-rule";
	private final UniqueKey ID;
	
	// All live instances of rules, should not be stored here
	// Only for mental model
	private static final ConditionDB database = new ConditionDB();
	
	protected void testConditions(Event someEvent) {
		for(Condition condition : database.getConditions(getClass())) {
			test(someEvent, condition);
		}
	}
	
	public abstract boolean test(Event someEvent, Condition someCondition);
	
	protected void registerConditions(Condition... conditions) {
		database.registerConditions(getClass(), conditions);
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
