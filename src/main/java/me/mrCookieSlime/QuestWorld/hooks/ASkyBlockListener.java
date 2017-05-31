package me.mrCookieSlime.QuestWorld.hooks;

import java.util.UUID;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.quests.QuestOfflineListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.wasteofplastic.askyblock.events.IslandLevelEvent;

public class ASkyBlockListener implements Listener {

	public ASkyBlockListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	
	@EventHandler
	public void onWin(final IslandLevelEvent e) {
		QuestChecker.check(e.getPlayer(), e, "ASKYBLOCK_REACH_ISLAND_LEVEL", new QuestOfflineListener() {
			
			@Override
			public void onProgressCheck(UUID uuid, QuestManager manager, QuestMission task, Object event) {
				manager.setProgress(task, e.getLevel());
			}
		});
	}
}
