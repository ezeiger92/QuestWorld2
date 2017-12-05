package me.mrCookieSlime.QuestWorld.api.contract;

import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.annotation.Nullable;
import me.mrCookieSlime.QuestWorld.manager.Party;

public interface IPlayerStatus {
	boolean hasDeathEvent(IMission mission);
	Party getParty();
	int countQuests(@Nullable ICategory root, @Nullable QuestStatus status);
	boolean hasFinished(IQuest quest);
	int getProgress(IMission task);
	int getProgress(IQuest quest);
	int getProgress(ICategory category);
	QuestStatus getStatus(IQuest quest);
	long getCooldownEnd(IQuest quest);
	boolean hasCompletedTask(IMission task);
	boolean hasUnlockedTask(IMission task);
	
	String progressString(IQuest quest);
	String progressString();
}
