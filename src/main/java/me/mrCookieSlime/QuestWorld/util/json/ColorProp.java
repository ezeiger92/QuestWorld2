package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

import org.bukkit.ChatColor;

class ColorProp implements Prop {
	private ChatColor color;
	public ColorProp(ChatColor color) {
		this.color = color;
	}
	
	@Override
	public void apply(Map<String, String> properties) {
		properties.put("\"color\"", '"'+color.name().toLowerCase()+'"');
	}
}
