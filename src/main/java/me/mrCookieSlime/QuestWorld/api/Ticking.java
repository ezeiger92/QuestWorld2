package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;

public interface Ticking extends Manual {
	default int onTick(Player player, IMission mission) {
		return onManual(player, mission);
	}
}
