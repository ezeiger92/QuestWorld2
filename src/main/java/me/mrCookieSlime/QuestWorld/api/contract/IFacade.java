package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Collection;

public interface IFacade {
	ICategory createCategory(String name, int id);
	
	Collection<? extends ICategory> getCategories();
	ICategory getCategory(int id);
	
	long getLastSave();
	
	void deleteCategory(ICategory category);
	void deleteQuest(IQuest quest);
	void deleteMission(IMission mission);
}
