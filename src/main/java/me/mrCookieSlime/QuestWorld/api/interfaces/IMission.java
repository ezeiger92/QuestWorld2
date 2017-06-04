package me.mrCookieSlime.QuestWorld.api.interfaces;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;

public interface IMission extends IQuestingObject {
	public String getID();
	public int getAmount();
	public String getText();
	
	public ItemStack getMissionItem();
	public ItemStack getDisplayItem();
	
	public EntityType getEntity();
	public String getEntityName();
	public MissionType getType();

	public Location getLocation();

	public List<String> getDialogue();
	public String getCustomName();
	
	public long getTimeframe();
	
	public boolean hasTimeframe();

	public boolean resetsonDeath();

	public String getLore();

	public int getCustomInt();

	public boolean acceptsSpawners();
}
