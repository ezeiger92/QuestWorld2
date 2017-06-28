package me.mrCookieSlime.QuestWorld.api;

import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.events.CancellableEvent;
import me.mrCookieSlime.QuestWorld.events.MissionChangeEvent;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.utils.BitFlag;
import me.mrCookieSlime.QuestWorld.utils.BitFlag.BitString;

public class MissionChange extends Mission {
	public enum Member implements BitString {
		QUEST,
		TYPE,
		ITEM,
		AMOUNT,
		ID,
		ENTITY,
		LOCATION,
		NAME,
		DISPLAY_NAME,
		TIMEFRAME,
		DEATH_RESET,
		LORE,
		CUSTOM_INT,
		SPAWNERS_ALLOWED,
		DIALOGUE,
	}
	
	private long changeBits;
	private Mission origin;
	
	public MissionChange(Mission copy) {
		super(copy);
		changeBits = 0;
		origin = copy;
	}
	
	public boolean hasChange(Member field) {
		return (changeBits & BitFlag.getBits(field)) != 0;
	}
	
	public List<Member> getChanges() {
		return BitFlag.getFlags(Member.values(), changeBits);
	}
	
	public Mission getSource() {
		return origin;
	}
	
	public void apply() {
		copyTo(origin);
	}
	
	public boolean sendEvent() {
		if(changeBits == 0)
			return false;
		
		return CancellableEvent.send(new MissionChangeEvent(this));
	}
	
	// Modify "setX" methods
	@Override
	public void setItem(ItemStack item) {
		super.setItem(item);
		changeBits |= BitFlag.getBits(Member.ITEM);
	}

	@Override
	public void setEntity(EntityType entity) {
		super.setEntity(entity);
		changeBits |= BitFlag.getBits(Member.ENTITY);
	}

	@Override
	public void setEntityName(String name) {
		super.setEntityName(name);
		changeBits |= BitFlag.getBits(Member.NAME);
	}

	@Override
	public void setType(MissionType type) {
		super.setType(type);
		changeBits |= BitFlag.getBits(Member.TYPE);
	}

	@Override
	public void setAmount(int amount) {
		super.setAmount(amount);
		changeBits |= BitFlag.getBits(Member.AMOUNT);
	}

	@Override
	public void setLocation(Player p) {
		super.setLocation(p);
		changeBits |= BitFlag.getBits(Member.LOCATION);
	}

	@Override
	public void addDialogueLine(Player p, final String path) {
		super.addDialogueLine(p, path);
		changeBits |= BitFlag.getBits(Member.DIALOGUE);
	}
	
	@Override
	public void setCustomName(String name) {
		super.setCustomName(name);
		changeBits |= BitFlag.getBits(Member.NAME);
	}

	@Override
	public void setTimeframe(long timeframe) {
		super.setTimeframe(timeframe);
		changeBits |= BitFlag.getBits(Member.TIMEFRAME);
	}

	@Override
	public void setDeathReset(boolean deathReset) {
		super.setDeathReset(deathReset);
		changeBits |= BitFlag.getBits(Member.DEATH_RESET);
	}

	@Override
	public void setLore(String lore) {
		super.setLore(lore);
		changeBits |= BitFlag.getBits(Member.LORE);
	}

	@Override
	public void setCustomInt(int val) {
		super.setCustomInt(val);
		changeBits |= BitFlag.getBits(Member.CUSTOM_INT);
	}

	@Override
	public void setSpawnerSupport(boolean acceptsSpawners) {
		super.setSpawnerSupport(acceptsSpawners);
		changeBits |= BitFlag.getBits(Member.SPAWNERS_ALLOWED);
	}

}
