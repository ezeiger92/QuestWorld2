package me.mrCookieSlime.QuestWorld.api;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.events.CancellableEvent;
import me.mrCookieSlime.QuestWorld.events.QuestChangeEvent;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.utils.BitFlag;
import me.mrCookieSlime.QuestWorld.utils.BitFlag.BitString;

public class QuestChange extends Quest {
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
	
	private long changeBits;
	private Quest origin;
	
	public QuestChange(Quest copy) {
		super(copy);
		
		changeBits = 0;
		origin = copy;
	}
	
	public boolean hasChange(Member field) {
		return (changeBits & BitFlag.getBits(field)) != 0;
	}

	/**
	 * Get a list representing the members changed in this Quest
	 * 
	 * @return Member enum list
	 */
	public List<Member> getChanges() {
		return BitFlag.getFlags(Member.values(), changeBits);
	}
	
	/**
	 * Get the quest these changes apply to
	 * 
	 * @return Origin quest
	 */
	public Quest getSource() {
		return origin;
	}
	
	/**
	 * Applies all changes held by this object to the original quest
	 */
	public void apply() {
		copyTo(origin);
	}
	
	/**
	 * Fires a QuestChangeEvent
	 * 
	 * @see QuestChangeEvent
	 * @return false if the event is cancelled, otherwise true
	 */
	public boolean sendEvent() {
		return CancellableEvent.send(new QuestChangeEvent(this));
	}
	
	// Modify "setX" methods
	@Override
	public void setItemRewards(Player p) {
		super.setItemRewards(p);
		changeBits |= BitFlag.getBits(Member.REWARDS);
	}

	@Override
	public void setItem(ItemStack item) {
		super.setItem(item);
		changeBits |= BitFlag.getBits(Member.ITEM);
	}
	
	@Override
	public void toggleWorld(String world) {
		super.toggleWorld(world);
		changeBits |= BitFlag.getBits(Member.WORLD_BLACKLIST);
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		changeBits |= BitFlag.getBits(Member.NAME);
	}
	
	@Override
	public void addMission(Mission mission) {
		super.addMission(mission);
		changeBits |= BitFlag.getBits(Member.TASKS);
	}
	
	@Override
	public void removeMission(Mission mission) {
		super.removeMission(mission);
		changeBits |= BitFlag.getBits(Member.TASKS);
	}
	
	@Override
	public void setPartySize(int size) {
		super.setPartySize(size);
		changeBits |= BitFlag.getBits(Member.PARTYSIZE);
	}
	
	@Override
	public void setRawCooldown(long cooldown) {
		super.setRawCooldown(cooldown);
		changeBits |= BitFlag.getBits(Member.COOLDOWN);
	}
	
	@Override
	public void setCooldown(long cooldown) {
		super.setCooldown(cooldown);
		changeBits |= BitFlag.getBits(Member.COOLDOWN);
	}
	
	@Override
	public void setMoney(int money) {
		super.setMoney(money);
		changeBits |= BitFlag.getBits(Member.MONEY);
	}
	
	@Override
	public void setXP(int xp) {
		super.setXP(xp);
		changeBits |= BitFlag.getBits(Member.XP);
	}
	
	@Override
	public void setParent(Quest quest) {
		super.setParent(quest);
		changeBits |= BitFlag.getBits(Member.PARENT);
	}
	
	@Override
	public void removeCommand(int i) {
		super.removeCommand(i);
		changeBits |= BitFlag.getBits(Member.COMMANDS);
	}

	@Override
	public void addCommand(String command) {
		super.addCommand(command);
		changeBits |= BitFlag.getBits(Member.COMMANDS);
	}
	
	@Override
	public void setPermission(String permission) {
		super.setPermission(permission);
		changeBits |= BitFlag.getBits(Member.PERMISSION);
	}
	
	@Override
	public void setPartySupport(boolean supportsParties) {
		super.setPartySupport(supportsParties);
		changeBits |= BitFlag.getBits(Member.DISABLEPARTIES);
	}
	
	@Override
	public void setOrdered(boolean ordered) {
		super.setOrdered(ordered);
		changeBits |= BitFlag.getBits(Member.ORDERED);
	}
	
	@Override
	public void setAutoClaim(boolean autoclaim) {
		super.setAutoClaim(autoclaim);
		changeBits |= BitFlag.getBits(Member.AUTOCLAIM);
	}
	
}
