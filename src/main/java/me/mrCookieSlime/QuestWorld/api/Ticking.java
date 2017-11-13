package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.entity.Player;

public interface Ticking extends Manual {
	default void onTick(Player player, MissionSet.Result mission) {
		onManual(player, mission);
	}
}
