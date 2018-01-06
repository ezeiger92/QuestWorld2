package me.mrCookieSlime.QuestWorld.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import me.mrCookieSlime.QuestWorld.api.contract.IStateful;
import me.mrCookieSlime.QuestWorld.api.event.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.api.event.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.api.event.QuestDeleteEvent;
import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;

public class MenuListener implements Listener {

	@EventHandler(ignoreCancelled=true)
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getInventory().getHolder() instanceof Menu) {
			Menu openMenu = (Menu)event.getInventory().getHolder();
			boolean val = true;
			try {
				val = openMenu.click(event);
			}
			catch(Throwable e) {
				e.printStackTrace();
				event.getWhoClicked().sendMessage(ChatColor.RED + "An internal error occurred, please contact an admin!");
			}
			event.setCancelled(val);
		}
	}
	
	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if(event.getInventory().getHolder() instanceof Menu) {
			Menu openMenu = (Menu) event.getInventory().getHolder();
			for(int key : event.getRawSlots())
				if(openMenu.requestCancel(event.getInventory(), key)) {
					event.setCancelled(true);
					return;
				}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onCategoryDelete(CategoryDeleteEvent event) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			IStateful object = QuestBook.getLastViewed(p);
			if(event.getCategory() == object ||
					event.getCategory().getQuests().contains(object))
				QuestBook.setLastViewed(p, null);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onQuestDelete(QuestDeleteEvent event) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			IStateful object = QuestBook.getLastViewed(p);
			if(event.getQuest() == object)
				QuestBook.setLastViewed(p, null);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onMissionDelete(MissionDeleteEvent event) {
	}
}
