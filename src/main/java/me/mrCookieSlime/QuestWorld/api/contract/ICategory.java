package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Collection;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface ICategory extends IStateful {
	int getID();
	boolean isHidden();
	String getName();
	String getPermission();
	
	ItemStack getItem();
	IQuest getParent();
	Collection<? extends IQuest> getQuests();
	// getWorlds

	IQuest getQuest(int i);
	boolean isWorldEnabled(String world);

	@Override
	ICategoryState getState();
	@Deprecated
	void save(boolean force);
}
