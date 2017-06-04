package me.mrCookieSlime.QuestWorld.quests;

import java.util.UUID;

import me.mrCookieSlime.QuestWorld.QuestWorld;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class QuestChecker {
	
	//TODO: Holy hell there is so much of this running so frequently, whyyyyyyyyyyyyyy
	public static void check(Player p, Event event, String type, QuestListener listener) {
		QuestManager manager = QuestWorld.getInstance().getManager(p);
		for (Category category: QuestWorld.getInstance().getCategories()) {
			if (category.isWorldEnabled(p.getWorld().getName())) {
				for (Quest quest: category.getQuests()) {
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						for (Mission task: quest.getMissions()) {
							if (!manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
								if (task.getType().getID().equals(type)) listener.onProgressCheck(p, manager, task, event);
							}
						}
					}
				}
			}
		}
	}
	
	public static void check(UUID uuid, Event event, String type, QuestOfflineListener listener) {
		QuestManager manager = QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(uuid));
		for (Category category: QuestWorld.getInstance().getCategories()) {
			for (Quest quest: category.getQuests()) {
				if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE)) {
					for (Mission task: quest.getMissions()) {
						if (!manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
							if (task.getType().getID().equals(type)) listener.onProgressCheck(uuid, manager, task, event);
						}
					}
				}
			}
		}
	}

}
