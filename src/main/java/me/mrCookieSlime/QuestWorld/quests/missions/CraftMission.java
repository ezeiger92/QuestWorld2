package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class CraftMission extends MissionType {
	public CraftMission() {
		super("CRAFT", true, true, false, SubmissionType.ITEM, "Craft %s", new MaterialData(Material.WORKBENCH));
	}
	
	@Override
	public ItemStack getQuestItem(QuestMission qm) {
		return qm.getRawItem().clone();
	}

}
