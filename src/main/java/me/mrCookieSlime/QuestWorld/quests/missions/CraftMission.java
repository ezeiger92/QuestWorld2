package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class CraftMission extends MissionType {
	public CraftMission() {
		super("CRAFT", true, true, false, SubmissionType.ITEM, "&7Craft %s", new MaterialData(Material.WORKBENCH));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return qm.getRawItem().clone();
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		return "&7Craft " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getItem(), false);
	}

}
