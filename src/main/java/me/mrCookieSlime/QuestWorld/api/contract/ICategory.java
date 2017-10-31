package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface ICategory extends IRenderable {
	int getID();
	ItemStack getItem();
	IQuest getQuest(int i);
	Collection<? extends IQuest> getQuests();
	IQuest getParent();
	boolean isHidden();
	boolean isWorldEnabled(String world);
	
	String getName();
	boolean isValid();
	
	String getProgress(Player p);
	int countQuests(Player p, QuestStatus onCooldown);
	int countFinishedQuests(Player p);
	
	ICategoryWrite getState();
	void save(boolean force);
}
