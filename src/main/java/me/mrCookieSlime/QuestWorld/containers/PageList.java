package me.mrCookieSlime.QuestWorld.containers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class PageList {
	private final int pageSize;
	private final ItemStack[] items;
	private final MenuClickHandler[] buttons;
	
	private int filled = 0;
	private ItemStack defaultItem = null;
	private MenuClickHandler defaultButton = null;
	
	public PageList(int elementCount) {
		pageSize = elementCount;
		items = new ItemStack[pageSize];
		buttons = new MenuClickHandler[pageSize];
	}
	
	public PageList(int elementCount, ItemStack item, MenuClickHandler button) {
		this(elementCount);
		defaultItem = ItemBuilder.clone(item);
		defaultButton = button;
	}
	
	public void setDefaultItem(ItemStack item) {
		defaultItem = item.clone();
	}
	
	public void setDefaultButton(MenuClickHandler button) {
		defaultButton = button;
	}
	
	public ItemStack getItem(int index) {
		return ItemBuilder.clone(items[index]);
	}
	
	public MenuClickHandler getButton(int index) {
		return buttons[index];
	}
	
	public void addItem(int index, ItemStack item) {
		items[index] = ItemBuilder.clone(item);
	}
	
	public void addButton(int index, MenuClickHandler button) {
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
	
	public void build(ChestMenu menu) {
		build(menu, 0, pageSize);
	}
	
	public void build(ChestMenu menu, int offset) {
		build(menu, offset, pageSize);
	}
	
	public void build(ChestMenu menu, int offset, int activeSize) {
		for(int i = 0; i < activeSize; ++i) {
			int slot = i + offset;
			ItemStack item = items[i];
			if(item == null)
				item = defaultItem;
			
			MenuClickHandler button = buttons[i];
			if(button == null && item != null)
				button = defaultButton;

			menu.addItem(slot, ItemBuilder.clone(item));
			menu.addMenuClickHandler(slot, button);
		}
	}
}
