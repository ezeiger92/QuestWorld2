package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class DetectMission extends MissionType {
	public DetectMission() {
		super("DETECT", false, false, false, SubmissionType.ITEM, new MaterialData(Material.COMMAND));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return qm.getMissionItem().clone();
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		return "&7Own " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}
}
