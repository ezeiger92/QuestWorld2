package com.questworld.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;

import com.questworld.api.contract.DataObject;
import com.questworld.api.event.CategoryDeleteEvent;
import com.questworld.api.event.MissionDeleteEvent;
import com.questworld.api.event.QuestDeleteEvent;
import com.questworld.api.menu.Menu;
import com.questworld.api.menu.QuestBook;
import com.questworld.util.AutoListener;

public class MenuListener extends AutoListener {

	public MenuListener(Plugin plugin) {
		register(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof Menu) {
			Menu openMenu = (Menu) event.getInventory().getHolder();
			boolean val = true;
			try {
				val = openMenu.click(event);
			}
			catch (Throwable e) {
				event.getWhoClicked()
						.sendMessage(ChatColor.RED + "An internal error occurred, please contact an admin!");
				e.printStackTrace();
			}
			event.setCancelled(val);
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (event.getInventory().getHolder() instanceof Menu) {
			Menu openMenu = (Menu) event.getInventory().getHolder();
			for (int key : event.getRawSlots())
				if (openMenu.requestCancel(event.getInventory(), key)) {
					event.setCancelled(true);
					return;
				}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCategoryDelete(CategoryDeleteEvent event) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			DataObject object = QuestBook.getLastViewed(p);
			if (event.getCategory() == object || event.getCategory().getQuests().contains(object))
				QuestBook.setLastViewed(p, null);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onQuestDelete(QuestDeleteEvent event) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			DataObject object = QuestBook.getLastViewed(p);
			if (event.getQuest() == object)
				QuestBook.setLastViewed(p, null);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onMissionDelete(MissionDeleteEvent event) {
	}
}
