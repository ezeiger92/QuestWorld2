package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.QuestStatus;

public interface ICategory extends IQuestingObject {
	int getID();
	ItemStack getItem();
	IQuest getQuest(int i);
	Collection<? extends IQuest> getQuests();
	IQuest getParent();
	boolean isHidden();
	boolean isWorldEnabled(String world);
	
	String getProgress(Player p);
	int countQuests(Player p, QuestStatus onCooldown);
	int countFinishedQuests(Player p);
	
	ICategoryWrite getWriter();
	void save(boolean force);
}
