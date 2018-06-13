package com.questworld.api.contract;

import com.questworld.api.QuestStatus;
import com.questworld.api.annotation.NoImpl;
import com.questworld.api.annotation.Nullable;

@NoImpl
public interface IPlayerStatus {
	boolean hasDeathEvent(IMission mission);

	int countQuests(@Nullable ICategory category, @Nullable QuestStatus status);

	boolean hasFinished(IQuest quest);

	int getProgress(IMission mission);

	int getProgress(IQuest quest);

	int getProgress(ICategory category);

	QuestStatus getStatus(IQuest quest);

	long getCooldownEnd(IQuest quest);

	boolean isMissionActive(IMission mission);

	boolean hasCompletedTask(IMission mission);

	boolean hasUnlockedTask(IMission mission);

	void update();

	String progressString(IQuest quest);

	String progressString();
}
