package com.questworld.newquest;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;

public abstract class Rule {
	private final NamespacedKey ID;
	
	protected Rule(NamespacedKey ID) {
		this.ID = ID;
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
