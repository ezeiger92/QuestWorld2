package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Collection;

public interface IFacade {
	ICategory createCategory(String name, int id);
	IQuest createQuest(String name, int id, ICategory category);
	IMission createMission(int id, IQuest quest);
	Collection<? extends ICategory> getCategories();
	ICategory getCategory(int id);
	
	long getLastSave();
	
	// WARN
	void registerCategory(ICategory category);
	
	// WARN
	void unregisterCategory(ICategory category);
}
