package me.mrCookieSlime.QuestWorld.api;

import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;

public interface Manual {
	public static int FAIL = -1;
	int onManual(PlayerManager manager, IMission mission);
	default String getLabel() {
		return "Check";
	};
}
