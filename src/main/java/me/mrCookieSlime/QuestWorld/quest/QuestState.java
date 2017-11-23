package me.mrCookieSlime.QuestWorld.quest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.event.QuestChangeEvent;
import me.mrCookieSlime.QuestWorld.util.BitFlag;

class QuestState extends Quest {	
	private long changeBits = 0;
	private Quest origin;
	
	public QuestState(Quest copy) {
		super(copy);
		setUnique(copy.getUnique());
		origin = copy;
	}

	@Override
	public QuestState getState() {
		return this;
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
	public void addMission(int index) {
		super.addMission(index);
		changeBits |= BitFlag.getBits(Member.TASKS);
	}
	
	@Override
	public void removeMission(IMission mission) {
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
	public void setParent(IQuest quest) {
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
	
	/**
	 * Applies all changes held by this object to the original quest
	 */
	@Override
	public boolean apply() {
		if(sendEvent()) {
			copyTo(origin);
			origin.updateLastModified();
			changeBits = 0;
			return true;
		}
		return false;
	}

	@Override
	public boolean discard() {
		if(changeBits != 0) {
			copy(origin);
			changeBits = 0;
			return true;
		}
		return false;
	}
	
	/**
	 * Get the quest these changes apply to
	 * 
	 * @return Origin quest
	 */
	@Override
	public Quest getSource() {
		return origin;
	}
	
	@Override
	public boolean hasChange(Member field) {
		return (changeBits & BitFlag.getBits(field)) != 0;
	}
	
	/**
	 * Fires a QuestChangeEvent
	 * 
	 * @see QuestChangeEvent
	 * @return false if the event is cancelled, otherwise true
	 */
	public boolean sendEvent() {
		return changeBits != 0 && CancellableEvent.send(new QuestChangeEvent(this));
	}
}
