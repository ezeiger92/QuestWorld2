package me.mrCookieSlime.QuestWorld.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.event.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.QuestDeleteEvent;
import me.mrCookieSlime.QuestWorld.manager.MenuManager;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;

public class MenuListener implements Listener {

	@EventHandler(ignoreCancelled=true)
	public void onInventoryClick(InventoryClickEvent event) {
		Menu openMenu = MenuManager.get().playerOpenMenus.get(event.getWhoClicked().getUniqueId());
		if(openMenu != null) {
			boolean val = true;
			try {
				val = openMenu.click(event);
			}
			catch(Exception e) {
				e.printStackTrace();
				event.getWhoClicked().sendMessage(ChatColor.RED + "An internal error occurred, please contact an admin!");
			}
			event.setCancelled(val);
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		MenuManager.get().playerOpenMenus.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		Menu openMenu = MenuManager.get().playerOpenMenus.get(event.getWhoClicked().getUniqueId());
		if(openMenu != null)
			for(int key : event.getRawSlots())
				if(openMenu.requestCancel(event.getInventory(), key)) {
					event.setCancelled(true);
					return;
				}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onCategoryDelete(CategoryDeleteEvent event) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerManager manager = PlayerManager.of(p);
			if(event.getCategory() == manager.getLastEntry() ||
					event.getCategory().getQuests().contains(manager.getLastEntry()))
				manager.setLastEntry(null);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onQuestDelete(QuestDeleteEvent event) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerManager manager = PlayerManager.of(p);
			if(event.getQuest() == manager.getLastEntry())
				manager.setLastEntry(event.getQuest().getCategory());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onMissionDelete(MissionDeleteEvent event) {
	}
}
