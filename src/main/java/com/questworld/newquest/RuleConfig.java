package com.questworld.newquest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RuleConfig {
	private final Rule rule;
	private final Map<String, Object> properties;
	
	protected RuleConfig(Rule rule) {
		this.rule = rule;
		properties = new HashMap<>();
	}
	
	public final <T> T getProperty(String key, Function<Object, T> transform) {
		return transform.apply(properties.get(key));
	}
	
	public final Object getProperty(String key) {
		return properties.get(key);
	}
}
