package com.questworld.api.context;

import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IPlayerStatus;
import com.questworld.api.contract.IQuest;

public class ProgressContext extends MultiContext {
	public ProgressContext(IPlayerStatus status, IMission mission) {
		super("progress", new PlayerContext(status.getPlayer()),
				new MissionContext(mission));
		
		map("current", () -> status.getProgress(mission));
		map("total", mission::getAmount);
		map("remaining", () -> mission.getAmount() - status.getProgress(mission));
	}
	
	public ProgressContext(IPlayerStatus status, IQuest quest) {
		super("progress", new PlayerContext(status.getPlayer()),
				new QuestContext(quest));
		
		map("current", () -> status.getProgress(quest));
		map("total", () -> quest.getMissions().size());
		map("remaining", () -> quest.getMissions().size() - status.getProgress(quest));
	}
	
	public ProgressContext(IPlayerStatus status, ICategory category) {
		super("progress", new PlayerContext(status.getPlayer()),
				new CategoryContext(category));
		
		map("current", () -> status.getProgress(category));
		map("total", () -> category.getQuests().size());
		map("remaining", () -> category.getQuests().size() - status.getProgress(category));
	}
}
