package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.entity.Player;

public interface Manual {
	//public static int FAIL = -1;
	
	void onManual(Player player, MissionSet.Result mission);
	
	default String getLabel() {
		return "Check";
	};
}
