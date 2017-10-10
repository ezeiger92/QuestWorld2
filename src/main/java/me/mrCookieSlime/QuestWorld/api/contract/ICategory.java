package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Collection;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.quest.QuestStatus;

public interface ICategory extends IQuestingObject {
	int getID();
	ItemStack getItem();
	IQuest getQuest(int i);
	Collection<? extends IQuest> getQuests();
	IQuest getParent();
	boolean isHidden();
	boolean isWorldEnabled(String world);
	

	Set<? extends IQuest> getQuests(Player p, QuestStatus status);
	Set<? extends IQuest> getFinishedQuests(Player p);
	String getProgress(Player p);
}
