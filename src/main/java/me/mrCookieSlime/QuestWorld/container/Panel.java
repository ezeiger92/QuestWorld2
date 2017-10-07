package me.mrCookieSlime.QuestWorld.container;

import java.util.function.Consumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;

public class Panel {
	private final int pageSize;
	private final ItemStack[] items;
	private final Consumer<InventoryClickEvent>[] buttons;
	
	private int filled = 0;
	
	@SuppressWarnings("unchecked")
	public Panel(int elementCount) {
		pageSize = elementCount;
		items = new ItemStack[pageSize];
		buttons = new Consumer[pageSize];
	}
	
	public ItemStack getItem(int index) {
		return ItemBuilder.clone(items[index]);
	}
	
	public Consumer<InventoryClickEvent> getButton(int index) {
		return buttons[index];
	}
	
	public void addButton(int index, ItemStack item, Consumer<InventoryClickEvent> button) {
		if(buttons[index] == null)
			++filled;
		items[index] = ItemBuilder.clone(item);
		buttons[index] = button;
	}
	
	public void removeItem(int index) {
		items[index] = null;
	}
	
	public void removeButton(int index) {
		if(buttons[index] != null)
			--filled;
		buttons[index] = null;
	}
	
	public int getFill() {
		return filled;
	}
	
	public void build(Menu menu) {
		build(menu, 0, pageSize);
	}
	
	public void build(Menu menu, int offset, int activeSize) {
		for(int i = 0; i < activeSize; ++i)
			menu.put(i + offset, items[i], buttons[i]);
	}
}
