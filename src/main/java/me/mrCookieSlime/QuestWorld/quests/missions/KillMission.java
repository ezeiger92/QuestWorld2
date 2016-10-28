package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class KillMission extends MissionType {
	public KillMission() {
		super("KILL", true, true, false, SubmissionType.ENTITY, "Kill %s", new MaterialData(Material.IRON_SWORD));
	}
}
