package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class LocationMission extends MissionType {
	public LocationMission() {
		super("REACH_LOCATION", false, false, true, SubmissionType.LOCATION, "Travel to %s", new MaterialData(Material.LEATHER_BOOTS));
	}
}
