package com.questworld.newquest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class NodeConfig {
	private final Map<String, Object> properties;
	
	protected NodeConfig() {
		properties = new HashMap<>();
	}
	
	public final <T> T getProperty(String key, Function<Object, T> transform) {
		return transform.apply(properties.get(key));
	}
	
	public final Object getProperty(String key) {
		return properties.get(key);
	}
	
	// Grab fancy serialization from ChromaLib
	public final <T> T deserialize(Class<T> clazz) {
		return null;
	}
	
	public final <T> void serialize(T instance) {
	}
}
