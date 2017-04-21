package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class JoinMission extends MissionType {
	public JoinMission() {
		super("JOIN", true, false, false, SubmissionType.INTEGER, "Join %s times",
				new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get().getData());
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return new ItemStack(Material.WATCH);
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		return "&7Join " + instance.getAmount() + " times";
	}
}
