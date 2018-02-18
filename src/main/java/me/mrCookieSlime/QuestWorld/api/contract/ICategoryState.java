package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;
import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

@NoImpl
public interface ICategoryState extends ICategory {
	// setID
	void setHidden(boolean hidden);
	void setName(String name);
	void setPermission(String permission);
	void setItem(ItemStack item);
	void setParent(IQuest object);
	// setQuests
	// setWorlds
	
	void addQuest(String name, int id);
	void removeQuest(IQuest quest);
	void toggleWorld(String world);

	
	ICategory getSource();
	enum Member implements BitString {
		QUESTS,
		ID,
		NAME,
		ITEM,
		PARENT,
		PERMISSION,
		HIDDEN,
		WORLD_BLACKLIST,
	}
	boolean hasChange(Member field);
}
