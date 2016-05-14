package me.mrCookieSlime.QuestWorld.hooks;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestListener;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener {

	public VoteListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
