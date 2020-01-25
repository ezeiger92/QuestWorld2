package com.questworld.newquest;

import org.bukkit.event.Event;

import com.questworld.util.UniqueKey;

public abstract class Rule {
	private static final String NAMESPACE = "questworld-rule";
	private final UniqueKey ID;
	
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
	
	public abstract boolean test(Event event, Objective objective);
	
	public enum Result {
		ALLOWED,
		DENIED,
		OBJECTIVE_FAILURE,
		QUSET_SUCCESS,
		QUEST_FAILURE,
	}
}
