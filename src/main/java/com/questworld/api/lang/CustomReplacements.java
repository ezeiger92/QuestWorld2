package com.questworld.api.lang;

import java.util.HashMap;
import java.util.Map;

public class CustomReplacements implements PlaceholderSupply<Void> {
	private final Map<String, String> staticReplacements = new HashMap<String, String>();
	
	@Override
	public Class<Void> forClass() {
		return void.class;
	}

	@Override
	public String getReplacement(String forKey) {
		return staticReplacements.getOrDefault(forKey, "");
	}

	public CustomReplacements Add(String key, String value) {
		staticReplacements.put(key, value);
		return this;
	}
}
