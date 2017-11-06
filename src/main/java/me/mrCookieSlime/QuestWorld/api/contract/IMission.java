package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface IMission extends IStateful {
	boolean acceptsSpawners();
	int          getAmount();
	int          getCustomInt();
	String       getCustomString();
	String       getDescription();
	List<String> getDialogue();
	String       getDisplayName();
	EntityType   getEntity();
	int          getIndex();
	Location     getLocation();
	ItemStack    getMissionItem();
	IQuest       getQuest();
	int          getTimeframe();
	MissionType  getType();
	boolean      resetsonDeath();

	ItemStack     getDisplayItem();
	@Override
	IMissionState getState();
	String        getText();
	
	@Deprecated
	String getDialogueFilename();
}
