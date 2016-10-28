package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class MineMission extends MissionType {
	public MineMission() {
		super("MINE_BLOCK", true, true, false, SubmissionType.BLOCK, "Mine %s", new MaterialData(Material.IRON_PICKAXE));
	}
}
