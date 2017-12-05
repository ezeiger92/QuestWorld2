package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.event.entity.PlayerDeathEvent;

import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;

public interface Decaying {
	default void onDeath(PlayerDeathEvent event, MissionEntry result) {
		result.setProgress(0);
		return;
	}
}
