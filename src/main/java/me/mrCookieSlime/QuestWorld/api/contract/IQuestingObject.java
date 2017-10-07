package me.mrCookieSlime.QuestWorld.api.contract;

public interface IQuestingObject {
	long getLastModified();
	long getUnique();
	
	String getName();
	String getPermission();
	
	boolean isValid();
}
