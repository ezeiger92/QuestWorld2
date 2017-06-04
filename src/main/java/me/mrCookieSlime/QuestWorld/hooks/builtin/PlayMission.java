package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;

public class PlayMission extends MissionType {
	public PlayMission() {
		super("PLAY_TIME", false, false, true, SubmissionType.TIME, new MaterialData(Material.WATCH));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return new ItemStack(Material.WATCH);
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Play for " + (instance.getAmount() / 60) + "h " + (instance.getAmount() % 60) + "m";
	}
}
