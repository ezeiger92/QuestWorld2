package com.questworld.api.lang;

import com.questworld.api.QuestStatus;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IPlayerStatus;
import com.questworld.api.contract.IQuest;
import com.questworld.util.Text;

public class CategoryReplacements extends BaseReplacements<ICategory> {
	private final IPlayerStatus status;
	private final ICategory category;
	
	public CategoryReplacements(ICategory category) {
		this(null, category);
	}
	
	public CategoryReplacements(IPlayerStatus status, ICategory category) {
		super("category.");
		this.status = status;
		this.category = category;
	}
	
	@Override
	public Class<ICategory> forClass(){
		return ICategory.class;
	}

	@Override
	public String getReplacement(String base, String fullKey) {
		switch (base) {
			case "name":
				return category.getName();

			case "node":
			case "permission":
				return category.getPermission().split(" ")[0];

			case "permission_full":
				return category.getPermission();
				
			case "desc":
			case "permission_display": {
				String[] parts = category.getPermission().split(" ");
				return parts[parts.length - 1];
			}
			
			case "parent": {
				IQuest q = category.getParent();
				
				return q != null ? new QuestReplacements(status, q).getReplacement(fullKey.substring(7)) : "";
			}
		}
		
		if (status == null) {
			return "";
		}
		
		switch (base) {
			case "count_quests_locked_parent":
				return Integer.toString(status.countQuests(category, QuestStatus.LOCKED_PARENT));
				
			case "count_quests_locked_no_perm":
				return Integer.toString(status.countQuests(category, QuestStatus.LOCKED_NO_PERM));
				
			case "count_quests_locked_world":
				return Integer.toString(status.countQuests(category, QuestStatus.LOCKED_WORLD));
				
			case "count_quests_locked_no_party":
				return Integer.toString(status.countQuests(category, QuestStatus.LOCKED_NO_PARTY));
				
			case "count_quests_locked_party_size":
				return Integer.toString(status.countQuests(category, QuestStatus.LOCKED_PARTY_SIZE));
			
			case "available":
			case "count_quests_available":
				return Integer.toString(status.countQuests(category, QuestStatus.AVAILABLE));
				
			case "reward":
			case "count_quests_reward_claimable":
				return Integer.toString(status.countQuests(category, QuestStatus.REWARD_CLAIMABLE));
				
			case "cooldown":
			case "count_quests_on_cooldown":
				return Integer.toString(status.countQuests(category, QuestStatus.ON_COOLDOWN));
				
			case "count_quests_finished":
				return Integer.toString(status.countQuests(category, QuestStatus.FINISHED));
				
			case "total":
			case "count_quests":
				return Integer.toString(status.countQuests(category, null));
			
			case "completed":
			case "progress":
				return Integer.toString(status.getProgress(category));
				
			case "progress_bar":
				return Text.progressBar(status.getProgress(category), status.countQuests(category, null), null);
		}
		
		return "";
	}
}
