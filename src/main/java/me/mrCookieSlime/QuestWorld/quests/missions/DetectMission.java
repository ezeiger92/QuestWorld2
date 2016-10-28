package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class DetectMission extends MissionType {
	public DetectMission() {
		super("DETECT", false, false, false, SubmissionType.ITEM, "Own %s", new MaterialData(Material.COMMAND));
	}
}
