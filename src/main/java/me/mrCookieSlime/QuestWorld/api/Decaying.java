package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.event.entity.PlayerDeathEvent;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;

public interface Decaying {
	default int onDeath(PlayerDeathEvent event, IMission mission) {
		return 0;
	}
}
