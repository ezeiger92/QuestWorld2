package me.mrCookieSlime.QuestWorld;

import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.quests.Category;

@Deprecated
public class QuestWorld {
	private static QuestWorld instance = new QuestWorld();
	
	public static QuestWorld getInstance() {
		return instance;
	}
	
	public List<Category> getCategories() {
		List<Category> dummy = new ArrayList<>();
		for(ICategory source : me.mrCookieSlime.QuestWorld.api.QuestWorld.getFacade().getCategories())
			dummy.add(new Category(source));
		
		return dummy;
	}
}
