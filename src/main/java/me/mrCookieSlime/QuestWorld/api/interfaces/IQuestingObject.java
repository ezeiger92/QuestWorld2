package me.mrCookieSlime.QuestWorld.api.interfaces;

public interface IQuestingObject {
	long getLastModified();
	long getUnique();
	
	String getName();
	String getPermission();
	
	boolean isValid();
}
