package me.mrCookieSlime.QuestWorld.hooks;

import me.clip.chatreaction.events.ReactionWinEvent;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestListener;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatReactionListener implements Listener {

	public ChatReactionListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onWin(ReactionWinEvent e) {
		Player p = e.getWinner();
		
		QuestChecker.check(p, e, "CHATREACTION_WIN", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event) {
				manager.addProgress(task, 1);
			}
		});
	}
}
