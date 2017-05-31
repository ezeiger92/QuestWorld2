package me.mrCookieSlime.QuestWorld.quests.pluginmissions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.material.MaterialData;

import com.vexsoftware.votifier.model.VotifierEvent;

import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestListener;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

public class VoteMission extends MissionType implements Listener {
	public VoteMission() {
		super("VOTIFIER_VOTE", true, false, false, SubmissionType.INTEGER, new MaterialData(Material.DIAMOND));
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		return "&7Vote " + instance.getAmount() + " times";
	}
	
	@EventHandler
	public void onVote(VotifierEvent e) {
		@SuppressWarnings("deprecation")
		Player p = Bukkit.getPlayer(e.getVote().getUsername());
		if (p != null) {
			QuestChecker.check(p, e, "VOTIFIER_VOTE", new QuestListener() {
				
				@Override
				public void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event) {
					manager.addProgress(task, 1);
				}
			});
		}
	}
}
