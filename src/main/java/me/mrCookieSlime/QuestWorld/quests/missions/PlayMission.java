package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class PlayMission extends MissionType {
	public PlayMission() {
		super("PLAY_TIME", false, false, true, SubmissionType.TIME, "Play for %s", new MaterialData(Material.WATCH));
	}
	
	@Override
	public ItemStack getQuestItem(QuestMission qm) {
		return new ItemStack(Material.WATCH);
	}
}
