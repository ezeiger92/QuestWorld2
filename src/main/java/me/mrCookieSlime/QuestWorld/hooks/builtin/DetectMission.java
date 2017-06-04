package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;

public class DetectMission extends MissionType {
	public DetectMission() {
		super("DETECT", false, false, false, SubmissionType.ITEM, new MaterialData(Material.COMMAND));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Own " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}
}
