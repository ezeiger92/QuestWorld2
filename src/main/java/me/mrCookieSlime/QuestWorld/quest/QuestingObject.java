package me.mrCookieSlime.QuestWorld.quest;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.contract.IQuestingObject;

public abstract class QuestingObject implements IQuestingObject {
	private long lastModified = System.currentTimeMillis();
	public long getLastModified() {
		return lastModified;
	}
	
	private static long s_uniqueId = 0;
	private long uniqueId = s_uniqueId++;
	public long getUnique() {
		return uniqueId;
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

	@Override
	public int hashCode() {
		return (int)getUnique();
	}
}
