package me.mrCookieSlime.QuestWorld.listeners;

import java.io.File;

import me.mrCookieSlime.CSCoreLibPlugin.events.ItemUseEvent;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Maps;
import me.mrCookieSlime.QuestWorld.GuideBook;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	
	public PlayerListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onQuestBook(ItemUseEvent e) {
		if (GuideBook.isSimilar(e.getItem())) {
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
	
	// Fix for ezeiger92/QuestWorld2#26
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		if(e.getClick() == ClickType.NUMBER_KEY) {
			ChestMenu menu = Maps.getInstance().menus.get(e.getWhoClicked().getUniqueId());
			
			if(menu != null)
				e.setCancelled(true);
		}
	}
}
