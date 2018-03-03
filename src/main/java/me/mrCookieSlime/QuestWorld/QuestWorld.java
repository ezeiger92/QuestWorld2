package me.mrCookieSlime.QuestWorld;

import java.util.Arrays;
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
		Object[] data = com.questworld.api.QuestWorld.getFacade().getCategories().toArray();
		int length = data.length;

		for(int i = 0; i < length; ++i)
			data[i] = new Category((ICategory) data[i]);

		return Arrays.asList((Category[]) data);
	}
}
