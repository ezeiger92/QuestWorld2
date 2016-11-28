package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;

public class KillMission extends MissionType {
	public KillMission() {
		super("KILL", true, true, false, SubmissionType.ENTITY, "Kill %s", new MaterialData(Material.IRON_SWORD));
	}
	
	@Override
	public ItemStack getQuestItem(QuestMission qm) {
		return SubmissionItemResolver.mobEgg(qm.getEntity());
	}
}
