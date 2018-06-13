package com.questworld.api.contract;

import java.util.Collection;

import org.bukkit.inventory.ItemStack;

import com.questworld.api.annotation.NoImpl;
import com.questworld.api.annotation.Nullable;

@NoImpl
public interface ICategory extends DataObject {
	int getID();

	boolean isHidden();

	String getName();

	String getPermission();

	ItemStack getItem();

	IQuest getParent();

	Collection<? extends IQuest> getQuests();
	// getWorlds

	@Nullable("No quest at index")
	IQuest getQuest(int index);

	boolean isWorldEnabled(String world);

	ICategoryState getState();
}
