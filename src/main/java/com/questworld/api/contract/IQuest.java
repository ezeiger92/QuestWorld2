package com.questworld.api.contract;

import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.annotation.NoImpl;

@NoImpl
public interface IQuest extends DataObject {
	public static final long COOLDOWN_SCALE = 60 * 1000;
	
	int getID();
	List<? extends IMission> getOrderedMissions();
	Collection<? extends IMission> getMissions();
	ItemStack getItem();
	ICategory getCategory();
	List<ItemStack> getRewards();
	IMission getMission(int i);
	long getRawCooldown();
	long getCooldown();
	int getMoney();
	int getPartySize();
	int getXP();
	List<String> getCommands();
	boolean supportsParties();
	boolean getOrdered();
	boolean getAutoClaimed();
	boolean getWorldEnabled(String world);
	String getFormattedCooldown();
	String getPermission();

	IQuest getParent();
	String getName();
	
	boolean completeFor(Player p);
	
	IQuestState getState();
}
