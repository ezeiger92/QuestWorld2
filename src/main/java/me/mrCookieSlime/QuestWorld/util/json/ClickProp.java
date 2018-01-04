package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

class ClickProp implements Prop {
	private String subKey;
	private String command;
	public ClickProp(String subKey, String command) {
		this.subKey = subKey;
		this.command = command;
	}

	@Override
	public void apply(Map<String, String> properties) {
		properties.put(Prop.CLICK.key, "{\"action\":"+subKey+",\"value\":\"" + command + "\"}");
	}
}
