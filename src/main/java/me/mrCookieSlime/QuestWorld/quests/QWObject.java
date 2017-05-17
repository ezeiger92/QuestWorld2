package me.mrCookieSlime.QuestWorld.quests;

import org.bukkit.entity.Player;

public abstract class QWObject {
	private long lastModified = System.currentTimeMillis();
	public long getLastModified() {
		return lastModified;
	}
	
	public void updateLastModified() {
		lastModified = System.currentTimeMillis();
	}
	
	public abstract String getName();
	public abstract void setParent(Quest quest);
	
	public abstract String getPermission();
	public abstract void setPermission(String permission);
	
	public boolean hasPermission(Player p) {
		String permission = getPermission();
		return permission.equals("") ? true: p.hasPermission(permission);
	}
	
	public boolean isValid() {
		return false;
	}

}
