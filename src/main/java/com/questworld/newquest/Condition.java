package com.questworld.newquest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Condition {
	private final Rule rule;
	private final Map<String, Object> properties;
	
	protected Condition(Rule rule) {
		this.rule = rule;
		properties = new HashMap<>();
	}
	
	protected final <T> T getProperty(String key, Function<Object, T> transform) {
		return transform.apply(properties.get(key));
	}
	
	protected final Object getProperty(String key) {
		return properties.get(key);
	}
}
