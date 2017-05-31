package me.mrCookieSlime.QuestWorld.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.mrCookieSlime.QuestWorld.events.QuestChangeEvent;

public class SelfListener implements Listener {

	@EventHandler
	public void onQuestModify(QuestChangeEvent e) {
		Bukkit.getLogger().info("Changing: " + e.getNextState().getChanges().toString());
		
		if(Math.random() > 0.6) {
			e.setCancelled(true);
			Bukkit.getLogger().info("Random cancel!");
		}
	}
}
