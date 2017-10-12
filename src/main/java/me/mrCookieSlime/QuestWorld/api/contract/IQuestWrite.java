package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;
import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

@NoImpl
public interface IQuestWrite extends IQuest {
	void setItemRewards(Player p);
	void setItem(ItemStack item);
	void toggleWorld(String world);
	void setName(String name);
	void addMission(IMission mission);
	void removeMission(IMission mission);
	void setPartySize(int size);
	void setRawCooldown(long cooldown);
	void setCooldown(long cooldown);
	void setMoney(int money);
	void setXP(int xp);
	void setParent(IQuest object);
	void removeCommand(int i);
	void addCommand(String command);
	void setPermission(String permission);
	void setPartySupport(boolean supportsParties);
	void setOrdered(boolean ordered);
	void setAutoClaim(boolean autoclaim);
	
	boolean apply();
	boolean discard();
	IQuest getSource();
	
	void refreshParent();
	
	public enum Member implements BitString {
		CATEGORY,
		ID,
		COOLDOWN,
		NAME,
		ITEM,
		TASKS,
		COMMANDS,
		WORLD_BLACKLIST,
		REWARDS,
		MONEY,
		XP,
		PARTYSIZE,
		DISABLEPARTIES,
		ORDERED,
		AUTOCLAIM,
		PARENT,
		PERMISSION,
	}
	boolean hasChange(Member field);
}
