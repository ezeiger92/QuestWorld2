package me.mrCookieSlime.QuestWorld.api.interfaces;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;

public interface IMissionWrite extends IMission {
	void setItem(ItemStack item);
	void setEntity(EntityType entity);
	void setCustomString(String customString);
	void setType(MissionType type);
	void setAmount(int amount);
	void setLocation(Player p);
	void addDialogueLine(Player p, final String path);
	void setDisplayName(String displayName);
	void setTimeframe(int timeframe);
	void setDeathReset(boolean deathReset);
	void setDescription(String description);
	void setCustomInt(int customInt);
	void setSpawnerSupport(boolean acceptsSpawners);
}
