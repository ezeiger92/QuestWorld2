package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface IRenderable {	
	String getPermission();
	
	default boolean checkPermission(Player p) {
		String permission = getPermission();
		return permission.equals("") ? true: p.hasPermission(permission);
	}
}
