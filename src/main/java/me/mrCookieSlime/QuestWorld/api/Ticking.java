package me.mrCookieSlime.QuestWorld.api;

import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;

public interface Ticking {
	boolean onTick(PlayerManager manager, IMission mission);
}
