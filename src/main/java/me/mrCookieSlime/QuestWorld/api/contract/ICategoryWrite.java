package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;
import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

@NoImpl
public interface ICategoryWrite extends ICategory {
	void setItem(ItemStack item);
	void setName(String name);
	void setPermission(String permission);
	void setHidden(boolean hidden);
	void toggleWorld(String world);
	void setParent(IQuest object);
	void addQuest(IQuest quest);
	void removeQuest(IQuest quest);
	
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
	boolean hasChange(Member field);
}
