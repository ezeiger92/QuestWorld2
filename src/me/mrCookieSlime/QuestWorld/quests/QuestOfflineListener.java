package me.mrCookieSlime.QuestWorld.quests;

import java.util.UUID;

public interface QuestOfflineListener {
	
	void onProgressCheck(UUID uuid, QuestManager manager, QuestMission task, Object event);

}
