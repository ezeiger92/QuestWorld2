package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;

public class SubmitMission extends MissionType {
	public SubmitMission() {
		super("SUBMIT", false, false, false, SubmissionType.ITEM, new MaterialData(Material.CHEST));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Submit " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}
}
