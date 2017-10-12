package me.mrCookieSlime.QuestWorld.listener;

import java.io.File;

import me.mrCookieSlime.QuestWorld.GuideBook;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;

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
		for (ICategory category: QuestWorld.getInstance().getCategories()) {
			if (category.isWorldEnabled(p.getWorld().getName())) {
				for (IQuest quest: category.getQuests()) {
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						for (IMission task: quest.getMissions()) {
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
		if (!new File("data-storage/Quest World/" + e.getPlayer().getUniqueId() + ".yml").exists() && QuestWorld.getInstance().getConfig().getBoolean("book.on-first-join"))
			e.getPlayer().getInventory().addItem(GuideBook.get());
	}
	
	@EventHandler
	public void onleave(PlayerQuitEvent e) {
		QuestWorld.getInstance().getManager(e.getPlayer()).unload();
	}
}
