package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

public interface ICategoryWrite extends ICategory {
	void setItem(ItemStack item);
	void setName(String name);
	//void setParent(Quest quest);
	void setPermission(String permission);
	void setHidden(boolean hidden);
	void toggleWorld(String world);
	
	boolean apply();
	boolean discard();
	ICategory getSource();
	
	public enum Member implements BitString {
		QUESTS,
		ID,
		NAME,
		ITEM,
		PARENT,
		PERMISSION,
		HIDDEN,
		WORLD_BLACKLIST,
	}
}
