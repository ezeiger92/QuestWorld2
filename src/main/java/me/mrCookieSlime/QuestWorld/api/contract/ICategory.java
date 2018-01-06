package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Collection;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;
import me.mrCookieSlime.QuestWorld.api.annotation.Nullable;

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

	@Nullable("No quest at index") IQuest getQuest(int index);
	boolean isWorldEnabled(String world);

	@Override
	ICategoryState getState();
}
