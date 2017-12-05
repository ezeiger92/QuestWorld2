package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;

public interface Manual {
	void onManual(Player player, MissionEntry mission);
	
	default String getLabel() {
		return "Check";
	};
}
