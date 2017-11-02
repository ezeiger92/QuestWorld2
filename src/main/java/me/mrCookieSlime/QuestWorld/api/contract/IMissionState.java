package me.mrCookieSlime.QuestWorld.api.contract;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;
import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

@NoImpl
public interface IMissionState extends IMission {
	void setItem(ItemStack item);
	void setEntity(EntityType entity);
	void setCustomString(String customString);
	void setType(MissionType type);
	void setAmount(int amount);
	void setLocation(Location loc);
	void setLocation(Player p);
	void setDisplayName(String displayName);
	void setTimeframe(int timeframe);
	void setDeathReset(boolean deathReset);
	void setDescription(String description);
	void setCustomInt(int customInt);
	void setSpawnerSupport(boolean acceptsSpawners);

	void setupDialogue(Player p);
	
	boolean apply();
	boolean discard();
	IMission getSource();
	
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
	boolean hasChange(Member field);
}
