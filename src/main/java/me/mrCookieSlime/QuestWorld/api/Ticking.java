package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;

public interface Ticking extends Manual {
	int onTick(Player player, IMission mission);
}
