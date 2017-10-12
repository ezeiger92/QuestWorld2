package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface IRenderable {
	long getLastModified();
	long getUnique();
	
	String getName();
	String getPermission();
	
	boolean hasPermission(Player p);
	
	boolean isValid();
	int hashCode();
}
