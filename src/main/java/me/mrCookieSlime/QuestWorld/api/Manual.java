package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;

public interface Manual {
	public static int FAIL = -1;
	
	int onManual(Player player, IMission mission);
	
	default String getLabel() {
		return "Check";
	};
}
