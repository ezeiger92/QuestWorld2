package me.mrCookieSlime.QuestWorld.listeners;

import java.io.File;

import me.mrCookieSlime.QuestWorld.GuideBook;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onQuestBook2(PlayerInteractEvent event) {
		Action a = event.getAction();
		if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)
			if (GuideBook.get().isSimilar(event.getItem()))
				QuestBook.openLastMenu(event.getPlayer());
	}
	
	@EventHandler
	public void onDie(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		PlayerManager manager = QuestWorld.getInstance().getManager(p);
		for (Category category: QuestWorld.getInstance().getCategories()) {
			if (category.isWorldEnabled(p.getWorld().getName())) {
				for (Quest quest: category.getQuests()) {
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						for (Mission task: quest.getMissions()) {
							if (task.resetsonDeath() && !manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
								manager.setProgress(task, 0);
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (!new File("data-storage/Quest World/" + e.getPlayer().getUniqueId() + ".yml").exists() && QuestWorld.getInstance().getCfg().getBoolean("book.on-first-join"))
			e.getPlayer().getInventory().addItem(GuideBook.get());
	}
	
	@EventHandler
	public void onleave(PlayerQuitEvent e) {
		QuestWorld.getInstance().getManager(e.getPlayer()).unload();
	}
}
