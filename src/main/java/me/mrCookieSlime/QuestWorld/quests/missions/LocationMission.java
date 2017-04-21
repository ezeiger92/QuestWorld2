package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;

public class LocationMission extends MissionType {
	public LocationMission() {
		super("REACH_LOCATION", false, false, true, SubmissionType.LOCATION, "Travel to %s", new MaterialData(Material.LEATHER_BOOTS));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return SubmissionItemResolver.location(Material.LEATHER_BOOTS, qm.getLocation());
	}
}
