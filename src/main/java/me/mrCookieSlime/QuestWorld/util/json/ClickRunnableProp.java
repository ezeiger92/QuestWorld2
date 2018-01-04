package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.command.ClickCommand;

public class ClickRunnableProp implements Prop {
	private UUID uuid;
	
	public ClickRunnableProp(Player p, Runnable callback) {
		uuid = ClickCommand.add(callback);
		
		ClickCommand.link(p.getUniqueId(), uuid);
	}
	
	@Override
	public void apply(Map<String, String> properties) {
		new ClickProp("\"run_command\"", "/qw-invoke " + uuid).apply(properties);
		
	}
}
