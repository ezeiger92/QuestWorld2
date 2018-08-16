package com.questworld.api.context;

import com.questworld.api.contract.IQuest;

public class QuestContext extends Context {
	public QuestContext(IQuest quest) {
		this(quest, "quest");
	}

	public QuestContext(IQuest quest, String prefix) {
		super(prefix);
		
		map(null, quest::getName);
		map("name", quest::getName);
		map("missions", () -> quest.getMissions().size());
		
		mapAll(new CategoryContext(quest.getCategory()).getMapping(), true);
	}
}
