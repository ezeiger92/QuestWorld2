package me.mrCookieSlime.QuestWorld.api.interfaces;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;

public interface IMissionWrite extends IMission {
	void setItem(ItemStack item);
	void setEntity(EntityType entity);
	void setEntityName(String name);
	void setType(MissionType type);
	void setAmount(int amount);
	void setLocation(Player p);
	void addDialogueLine(Player p, final String path);
	void setCustomName(String name);
	void setTimeframe(long timeframe);
	void setDeathReset(boolean deathReset);
	void setLore(String lore);
	void setCustomInt(int val);
	void setSpawnerSupport(boolean acceptsSpawners);
}
