package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.SkullType;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class JoinMission extends MissionType {
	public JoinMission() {
		super("JOIN", true, false, false, SubmissionType.INTEGER, "Join %s times",
				new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get().getData());
	}
}
