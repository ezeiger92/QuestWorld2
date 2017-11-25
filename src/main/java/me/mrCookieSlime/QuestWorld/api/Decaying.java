package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.event.entity.PlayerDeathEvent;

public interface Decaying {
	default void onDeath(PlayerDeathEvent event, MissionSet.Result result) {
		result.setProgress(0);
		return;
	}
}
