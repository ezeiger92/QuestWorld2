package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class SubmitMission extends MissionType {
	public SubmitMission() {
		super("SUBMIT", false, false, false, SubmissionType.ITEM, "Submit %s", new MaterialData(Material.CHEST));
	}
}
