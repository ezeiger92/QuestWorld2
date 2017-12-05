package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;

public interface Ticking extends Manual {
	default void onTick(Player player, MissionEntry mission) {
		onManual(player, mission);
	}
}
