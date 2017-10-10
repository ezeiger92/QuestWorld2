package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;

public interface IMission extends IQuestingObject {
	String getID();
	int getAmount();
	String getText();
	
	ItemStack getMissionItem();
	ItemStack getDisplayItem();
	
	EntityType getEntity();
	MissionType getType();

	Location getLocation();

	List<String> getDialogue();
	String getDisplayName();
	
	int getTimeframe();
	
	boolean hasTimeframe();

	boolean resetsonDeath();

	String getDescription();

	int getCustomInt();
	String getCustomString();

	boolean acceptsSpawners();
	IQuest getQuest();
}
