package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface IQuest extends IStateful {
	int getID();
	List<? extends IMission> getMissions();
	ItemStack getItem();
	ICategory getCategory();
	List<ItemStack> getRewards();
	IMission getMission(int i);
	int getRawCooldown();
	int getCooldown();
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
	
	void clearAllUserData();
	boolean completeFor(Player p);
	
	@Override
	IQuestState getState();
}
