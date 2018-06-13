package com.questworld.api.contract;

import java.util.Collection;
import java.util.UUID;

public interface IFacade {
	/**
	 * Creates a new category. Note that strings must not be colorized
	 * beforehand, otherwise there will be undefined behavior.
	 * <p>
	 * As of writing this, {@link com.questworld.util.Text#wrap Text.wrap}
	 * fails to apply text colors correctly, but at any point it may be changed
	 * to throw an IllegalArgumentException or perform some other behavior.
	 * 
	 * @see com.questworld.util.Text#colorize Text.colorize
	 * 
	 * @param name The category name. Do NOT use colorized strings
	 * @param id The category index in the quest book
	 * @return the newly created category
	 */
	ICategory createCategory(String name, int id);
	
	Collection<? extends ICategory> getCategories();
	
	ICategory getCategory(int index);
	
	long getLastSave();
	
	void deleteCategory(ICategory category);
	void deleteQuest(IQuest quest);
	void deleteMission(IMission mission);

	void clearAllUserData(ICategory category);
	void clearAllUserData(IQuest quest);

	ICategory getCategory(UUID uniqueId);
	IQuest getQuest(UUID uniqueId);
	IMission getMission(UUID uniqueId);
}
