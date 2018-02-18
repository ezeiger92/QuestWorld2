package me.mrCookieSlime.QuestWorld.quest;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.api.event.MissionChangeEvent;
import me.mrCookieSlime.QuestWorld.util.BitFlag;

class MissionState extends Mission {
	private long changeBits = 0;
	private Mission origin;
	
	public MissionState(Mission source) {
		super(source);
		origin = source;
	}
	
	//// IMissionWrite
	@Override
	public void setAmount(int amount) {
		super.setAmount(amount);
		changeBits |= BitFlag.getBits(Member.AMOUNT);
	}

	@Override
	public void setCustomInt(int val) {
		super.setCustomInt(val);
		changeBits |= BitFlag.getBits(Member.CUSTOM_INT);
	}

	@Override
	public void setCustomString(String customString) {
		super.setCustomString(customString);
		changeBits |= BitFlag.getBits(Member.CUSTOM_STRING);
	}

	@Override
	public void setDeathReset(boolean deathReset) {
		super.setDeathReset(deathReset);
		changeBits |= BitFlag.getBits(Member.DEATH_RESET);
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
		changeBits |= BitFlag.getBits(Member.DESCRIPTION);
	}
	
	@Override
	public void setDialogue(List<String> dialogue) {
		super.setDialogue(dialogue);
		changeBits |= BitFlag.getBits(Member.DIALOGUE);
	}
	
	@Override
	public void setDisplayName(String name) {
		super.setDisplayName(name);
		changeBits |= BitFlag.getBits(Member.CUSTOM_STRING);
	}

	@Override
	public void setEntity(EntityType entity) {
		super.setEntity(entity);
		changeBits |= BitFlag.getBits(Member.ENTITY);
	}
	
	@Override
	public void setItem(ItemStack item) {
		super.setItem(item);
		changeBits |= BitFlag.getBits(Member.ITEM);
	}
	
	@Override
	public void setLocation(Location loc) {
		super.setLocation(loc);
		changeBits |= BitFlag.getBits(Member.LOCATION);
	}

	@Override
	public void setSpawnerSupport(boolean acceptsSpawners) {
		super.setSpawnerSupport(acceptsSpawners);
		changeBits |= BitFlag.getBits(Member.SPAWNER_SUPPORT);
	}

	@Override
	public void setTimeframe(int timeframe) {
		super.setTimeframe(timeframe);
		changeBits |= BitFlag.getBits(Member.TIMEFRAME);
	}

	@Override
	public void setType(MissionType type) {
		super.setType(type);
		changeBits |= BitFlag.getBits(Member.TYPE);
	}
	
	@Override
	public void setIndex(int index) {
		super.setIndex(index);
		
		changeBits |= BitFlag.getBits(Member.INDEX);
	}
	
	private boolean applying = false;
	
	@Override
	public boolean apply() {
		
		// Prevent re-entry in case of validate() changing more settings.
		if(applying)
			return false;
		applying = true;
		validate();
		applying = false;
		
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
	
	@Override
	public Mission getSource() {
		return origin;
	}
	
	@Override
	public MissionState getState() {
		return this;
	}
	
	@Override
	public boolean hasChange(Member field) {
		return (changeBits & BitFlag.getBits(field)) != 0;
	}
	
	private boolean sendEvent() {
		return changeBits != 0 && CancellableEvent.send(new MissionChangeEvent(this));
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
