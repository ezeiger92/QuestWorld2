package me.mrCookieSlime.QuestWorld.quest;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IRenderable;

abstract class Renderable implements IRenderable {
	private static long s_uniqueId = 0;
	
	private final long uniqueId = s_uniqueId++;
	public final long getUnique() {
		return uniqueId;
	}
	
	private long lastModified = System.currentTimeMillis();
	public long getLastModified() {
		return lastModified;
	}
	
	public void updateLastModified() {
		lastModified = System.currentTimeMillis();
	}
	
	public abstract String getName();
	public abstract void setParent(IQuest quest);
	
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
