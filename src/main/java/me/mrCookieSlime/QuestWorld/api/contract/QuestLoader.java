package me.mrCookieSlime.QuestWorld.api.contract;

import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface QuestLoader {
	void attach(QuestExtension hook);
	void enable(QuestExtension hook);
}
