package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

class DefaultProp implements Prop {
	private String key;
	private String value;
	
	public DefaultProp(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public void apply(Map<String, String> properties) {
		properties.put(key, value);
	}
}
