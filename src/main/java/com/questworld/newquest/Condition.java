package com.questworld.newquest;

import java.util.Map;

import org.bukkit.event.Event;

public abstract class Condition {
	private final Rule rule;
	private Map<String, Object> properties;
	
	public Condition(Rule rule) {
		this.rule = rule;
	}
	
	// Needs some player context
	public abstract boolean test(Event someEvent);
}
