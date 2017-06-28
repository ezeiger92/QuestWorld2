package me.mrCookieSlime.QuestWorld.api.menu;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;

public class MenuData {
	private MenuClickHandler handler;
	private ItemStack item;
	
	public MenuData(ItemStack item, MenuClickHandler handler) {
		setItem(item);
		setHandler(handler);
	}
	
	public MenuClickHandler getHandler() {
		return handler;
	}
	
	public ItemStack getItem() {
		return item.clone();
	}
	
	public void setHandler(MenuClickHandler handler) {
		if(handler == null)
			throw new NullPointerException("Handler cannot be null!");
		
		this.handler = handler;
	}
	
	public void setItem(ItemStack item) {
		if(item == null)
			throw new NullPointerException("Item cannot be null!");
		
		this.item = item.clone();
	}
	
	public void attach(int slot, ChestMenu menu) {
		menu.addItem(slot, item, handler);
	}
}
