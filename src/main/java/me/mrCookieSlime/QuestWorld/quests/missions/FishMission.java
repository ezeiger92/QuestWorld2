package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class FishMission extends MissionType {
	public FishMission() {
		super("FISH", true, true, false, SubmissionType.ITEM, "Catch %s &7using a Fishing Rod", new MaterialData(Material.FISHING_ROD));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return qm.getRawItem().clone();
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		return "&7Fish up " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getItem(), false);
	}
}
