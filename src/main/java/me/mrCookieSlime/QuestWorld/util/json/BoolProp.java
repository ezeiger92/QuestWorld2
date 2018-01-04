package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

class BoolProp implements Prop {
	private boolean state;
	private String name;
	public BoolProp(String name, boolean state) {
		this.name = name;
		this.state = state;
	}
	
	@Override
	public void apply(Map<String, String> properties) {
		properties.put('"'+name+'"', "\""+state+"\"");
	}
}
