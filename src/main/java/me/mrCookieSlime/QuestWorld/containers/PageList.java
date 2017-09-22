package me.mrCookieSlime.QuestWorld.containers;

import java.util.function.Consumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class PageList {
	private final int pageSize;
	private final ItemStack[] items;
	private final Consumer<InventoryClickEvent>[] buttons;
	
	private int filled = 0;
	private ItemStack defaultItem = null;
	private Consumer<InventoryClickEvent> defaultButton = null;
	
	@SuppressWarnings("unchecked")
	public PageList(int elementCount) {
		pageSize = elementCount;
		items = new ItemStack[pageSize];
		buttons = new Consumer[pageSize];
	}
	
	public PageList(int elementCount, ItemStack item, Consumer<InventoryClickEvent> button) {
		this(elementCount);
		defaultItem = ItemBuilder.clone(item);
		defaultButton = button;
	}
	
	public void setDefaultItem(ItemStack item) {
		defaultItem = item.clone();
	}
	
	public void setDefaultButton(Consumer<InventoryClickEvent> button) {
		defaultButton = button;
	}
	
	public ItemStack getItem(int index) {
		return ItemBuilder.clone(items[index]);
	}
	
	public Consumer<InventoryClickEvent> getButton(int index) {
		return buttons[index];
	}
	
	public void addItem(int index, ItemStack item) {
		items[index] = ItemBuilder.clone(item);
	}
	
	public void addButton(int index, Consumer<InventoryClickEvent> button) {
		if(buttons[index] == null)
			++filled;
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
	
	public int getCapacity() {
		return pageSize;
	}
	
	public void build(Menu menu) {
		build(menu, 0, pageSize);
	}
	
	public void build(Menu menu, int offset) {
		build(menu, offset, pageSize);
	}
	
	public void build(Menu menu, int offset, int activeSize) {
		for(int i = 0; i < activeSize; ++i) {
			int slot = i + offset;
			ItemStack item = items[i];
			if(item == null)
				item = defaultItem;
			
			Consumer<InventoryClickEvent> button = buttons[i];
			if(button == null && item != null)
				button = defaultButton;

			menu.put(slot, ItemBuilder.clone(item), button);
		}
	}
}
