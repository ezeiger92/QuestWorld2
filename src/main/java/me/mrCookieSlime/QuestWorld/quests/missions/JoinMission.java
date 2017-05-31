package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestListener;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class JoinMission extends MissionType implements Listener {
	public JoinMission() {
		super("JOIN", true, false, false, SubmissionType.INTEGER,
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
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		QuestChecker.check(e.getPlayer(), e, "JOIN", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event) {
				manager.addProgress(task, 1);
			}
		});
	}
}
