package me.mrCookieSlime.QuestWorld;

import java.util.ArrayList;
import java.util.List;

import com.questworld.api.contract.ICategory;

import me.mrCookieSlime.QuestWorld.quests.Category;

@Deprecated
public class QuestWorld {
	private static QuestWorld instance = new QuestWorld();
	
	public static QuestWorld getInstance() {
		return instance;
	}
	
	public List<Category> getCategories() {
		List<Category> dummy = new ArrayList<>();
		for(ICategory source : com.questworld.api.QuestWorld.getFacade().getCategories())
			dummy.add(new Category(source));
		
		return dummy;
	}
}
