package me.mrCookieSlime.QuestWorld.quests;

import org.bukkit.entity.Player;

public interface QuestListener {
	
	void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event);

}
