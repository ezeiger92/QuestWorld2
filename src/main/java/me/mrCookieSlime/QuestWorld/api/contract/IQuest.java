package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface IQuest extends IRenderable {
	int getID();
	List<? extends IMission> getMissions();
	ItemStack getItem();
	ICategory getCategory();
	List<ItemStack> getRewards();
	IMission getMission(int i);
	long getRawCooldown();
	long getCooldown();
	int getMoney();
	int getPartySize();
	int getXP();
	IQuest getParent();
	List<String> getCommands();
	boolean supportsParties();
	boolean isOrdered();
	boolean isAutoClaiming();
	boolean isWorldEnabled(String world);
	String getFormattedCooldown();
	
	QuestStatus getStatus(Player p);
	int countFinishedTasks(Player p);
	void handoutReward(Player p);
	
	IQuestWrite getWriter();
}
