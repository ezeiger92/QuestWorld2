package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;
import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

@NoImpl
public interface IMissionState extends IMission {
	void setAmount(int amount);
	void setCustomString(String customString);
	void setCustomInt(int customInt);
	void setDeathReset(boolean deathReset);
	void setDescription(String description);
	void setDialogue(List<String> dialogue);
	void setDisplayName(String displayName);
	void setEntity(EntityType entity);
	void setItem(ItemStack item);
	void setLocation(Location loc);
	void setSpawnerSupport(boolean acceptsSpawners);
	void setType(MissionType type);
	void setTimeframe(int timeframe);

	boolean  apply();
	boolean  discard();
	IMission getSource();
	boolean  hasChange(Member field);
	
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
}
