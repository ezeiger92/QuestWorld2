package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class SubmitMission extends MissionType {
	public SubmitMission() {
		super("SUBMIT", false, false, false, SubmissionType.ITEM, "Submit %s", new MaterialData(Material.CHEST));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return qm.getRawItem().clone();
	}
}
