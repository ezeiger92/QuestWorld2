package me.mrCookieSlime.QuestWorld.api.contract;

import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

public interface IQuestWrite extends IQuest {
	
	boolean apply();
	boolean discard();
	IQuest getSource();
	
	public enum Member implements BitString {
		CATEGORY,
		ID,
		COOLDOWN,
		NAME,
		ITEM,
		TASKS,
		COMMANDS,
		WORLD_BLACKLIST,
		REWARDS,
		MONEY,
		XP,
		PARTYSIZE,
		DISABLEPARTIES,
		ORDERED,
		AUTOCLAIM,
		PARENT,
		PERMISSION,
	}
}
