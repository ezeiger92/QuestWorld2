package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.entity.Player;

public interface IQuestingObject {
	long getLastModified();
	long getUnique();
	
	String getName();
	String getPermission();
	
	boolean hasPermission(Player p);
	
	boolean isValid();
	int hashCode();
}
