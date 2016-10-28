package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;

public class FishMission extends MissionType {
	public FishMission() {
		super("FISH", true, true, false, SubmissionType.ITEM, "Catch %s &7using a Fishing Rod", new MaterialData(Material.FISHING_ROD));
	}
}
