package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;

public class LocationMission extends MissionType {
	public LocationMission() {
		super("REACH_LOCATION", false, false, true, SubmissionType.LOCATION, new MaterialData(Material.LEATHER_BOOTS));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return SubmissionItemResolver.location(Material.LEATHER_BOOTS, qm.getLocation());
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		Location loc = instance.getLocation();
		String locStr = instance.getName();
		if(locStr.isEmpty())
			locStr = String.format("X: %d, Y: %d, Z: %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			
		return "&7Travel to " + locStr;
	}
}
