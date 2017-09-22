package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.function.Consumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class MenuData {
	private Consumer<InventoryClickEvent> handler;
	private ItemStack item;
	
	public MenuData(ItemStack item, Consumer<InventoryClickEvent> handler) {
		setItem(item);
		setHandler(handler);
	}
	
	public Consumer<InventoryClickEvent> getHandler() {
		return handler;
	}
	
	public ItemStack getItem() {
		return ItemBuilder.clone(item);
	}
	
	public void setHandler(Consumer<InventoryClickEvent> handler) {
		this.handler = handler;
	}
	
	public void setItem(ItemStack item) {
		this.item = ItemBuilder.clone(item);
	}
}
