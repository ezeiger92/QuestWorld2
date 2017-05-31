package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class PlayMission extends MissionType {
	public PlayMission() {
		super("PLAY_TIME", false, false, true, SubmissionType.TIME, new MaterialData(Material.WATCH));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return new ItemStack(Material.WATCH);
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		return "&7Play for " + (instance.getAmount() / 60) + "h " + (instance.getAmount() % 60) + "m";
	}
}
