package com.questworld.api.context;

import com.questworld.api.contract.ICategory;

public class CategoryContext extends Context {
	public CategoryContext(ICategory category) {
		this(category, "category");
	}
	
	public CategoryContext(ICategory category, String prefix) {
		super(prefix);

		map(null, category::getName);
		map("name", category::getName);
		map("quests", () -> category.getQuests().size());
	}
}
