package me.mrCookieSlime.QuestWorld.listeners;

import java.io.File;

import me.mrCookieSlime.CSCoreLibPlugin.events.ItemUseEvent;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestListener;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	
	public PlayerListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onQuestBook(ItemUseEvent e) {
		if (QuestWorld.getInstance().isItemSimiliar(e.getItem(), QuestWorld.getInstance().guide)) {
			Player p = e.getPlayer();
			QuestBook.openLastMenu(p);
		}
	}
	
	@EventHandler
	public void onDie(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		QuestManager manager = QuestWorld.getInstance().getManager(p);
		for (Category category: QuestWorld.getInstance().getCategories()) {
			if (category.isWorldEnabled(p.getWorld().getName())) {
				for (Quest quest: category.getQuests()) {
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						for (QuestMission task: quest.getMissions()) {
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
		if (!new File("data-storage/Quest World/" + e.getPlayer().getUniqueId() + ".yml").exists() && QuestWorld.getInstance().getCfg().getBoolean("book.on-first-join")) e.getPlayer().getInventory().addItem(QuestWorld.getInstance().guide);
		
		QuestChecker.check(e.getPlayer(), e, "JOIN", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event) {
				manager.addProgress(task, 1);
			}
		});
	}
	
	@EventHandler
	public void onleave(PlayerQuitEvent e) {
		QuestWorld.getInstance().getManager(e.getPlayer()).unload();
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onMine(BlockBreakEvent e) {
		QuestManager manager = QuestWorld.getInstance().getManager(e.getPlayer());
		for (QuestMission task: QuestManager.block_breaking_tasks) {
			if (e.getBlock().getType().equals(task.getItem().getType())) {
				if (manager.getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && !manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
					manager.addProgress(task, 1);
				}
			}
		}
	}

}
