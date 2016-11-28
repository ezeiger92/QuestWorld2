package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class DetectMission extends MissionType {
	public DetectMission() {
		super("DETECT", false, false, false, SubmissionType.ITEM, "Own %s", new MaterialData(Material.COMMAND));
	}
	
	@Override
	public ItemStack getQuestItem(QuestMission qm) {
		return qm.getRawItem().clone();
	}
}
