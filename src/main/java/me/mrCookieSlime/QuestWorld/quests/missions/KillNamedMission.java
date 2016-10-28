package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class KillNamedMission extends MissionType {
	public KillNamedMission() {
		super("KILL_NAMED_MOB", true, true, false, SubmissionType.ENTITY, "Kill %s", new MaterialData(Material.GOLD_SWORD));
	}
}
