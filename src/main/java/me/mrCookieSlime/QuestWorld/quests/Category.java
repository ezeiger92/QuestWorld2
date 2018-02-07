package me.mrCookieSlime.QuestWorld.quests;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;

@Deprecated
public class Category {
	private ICategory source;
	public Category(ICategory copy) {
		source = copy;
	}
	
	public Set<Quest> getFinishedQuests(Player p) {
		Set<Quest> dummy = new HashSet<>();
		int len = QuestWorld.getPlayerStatus(p).getProgress(source);
		for(int i = 0; i < len; ++i)
			dummy.add(new Quest());
		
		return dummy;
	}
}
