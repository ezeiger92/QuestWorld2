package me.mrCookieSlime.QuestWorld.quests;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.questworld.api.QuestWorld;
import com.questworld.api.contract.ICategory;

@Deprecated
public class Category {
	private ICategory source;
	public Category(ICategory copy) {
		source = copy;
	}
	
	public Set<Quest> getFinishedQuests(Player p) {
		int len = QuestWorld.getPlayerStatus(p).getProgress(source);
		HashSet<Quest> dummy = new HashSet<>(len);
		
		for(int i = 0; i < len; ++i)
			dummy.add(new Quest());
		
		return dummy;
	}
}
