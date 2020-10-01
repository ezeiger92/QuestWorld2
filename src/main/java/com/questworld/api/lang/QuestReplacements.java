package com.questworld.api.lang;

import com.questworld.api.contract.IPlayerStatus;
import com.questworld.api.contract.IQuest;

public class QuestReplacements extends BaseReplacements<IQuest> {
	private final IPlayerStatus status;
	private final IQuest quest;
	
	public QuestReplacements(IQuest quest) {
		this(null, quest);
	}
	
	public QuestReplacements(IPlayerStatus status, IQuest quest) {
		super("quest.");
		this.status = status;
		this.quest = quest;
	}
	
	@Override
	public Class<IQuest> forClass(){
		return IQuest.class;
	}
	
	@Override
	public String getReplacement(String base, String fullKey) {
		switch (base) {
			case "name":
				return quest.getName();
				
			case "permission":
				return quest.getPermission().split(" ")[0];

			case "permission_full":
				return quest.getPermission();
				
			case "permission_display": {
				String[] parts = quest.getPermission().split(" ");
				return parts[parts.length - 1];
			}
			
			case "count_missions":
				return Integer.toString(quest.getMissions().size());
		}
		
		if (status == null) {
			return "";
		}
		
		switch (base) {
			case "progress":
				return Integer.toString(status.getProgress(quest));
				
			case "progressbar":
				return status.progressString(quest);
		}
		
		return "";
	}
}
