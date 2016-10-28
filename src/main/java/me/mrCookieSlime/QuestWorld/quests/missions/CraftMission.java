package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class CraftMission extends MissionType {

	public CraftMission() {
		super("CRAFT", true, true, false, SubmissionType.ITEM, "Craft %s", new MaterialData(Material.WORKBENCH));
	}

}
