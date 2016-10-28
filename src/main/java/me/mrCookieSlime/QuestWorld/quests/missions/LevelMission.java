package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class LevelMission extends MissionType {
	public LevelMission() {
		super("REACH_LEVEL", false, false, false, SubmissionType.INTEGER, "Reach Level %s", new MaterialData(Material.EXP_BOTTLE));
	}
}
