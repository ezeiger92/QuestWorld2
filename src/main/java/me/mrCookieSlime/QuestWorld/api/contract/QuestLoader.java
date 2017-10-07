package me.mrCookieSlime.QuestWorld.api.contract;

import me.mrCookieSlime.QuestWorld.api.QuestExtension;

public interface QuestLoader {
	void attach(QuestExtension hook);
	void enable(QuestExtension hook);
}
