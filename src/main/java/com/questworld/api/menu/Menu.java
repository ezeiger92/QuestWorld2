package com.questworld.api.menu;

import java.util.Arrays;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.questworld.util.ItemBuilder;
import com.questworld.util.Text;

public class Menu implements InventoryHolder {
	private static final int ROW_WIDTH = 9;
	private Inventory inv;
	private Consumer<InventoryClickEvent>[] handlers;
	
	@SuppressWarnings("unchecked")
	public Menu(int rows, String title) {
		int cells = rows * ROW_WIDTH;
		inv = makeInv(cells, Text.colorize(title));
		handlers = new Consumer[cells];
	}
	
	private Inventory makeInv(int cells, String title) {
		if(title != null)
			return Bukkit.createInventory(this, cells, Text.colorize(title));
		
		return Bukkit.createInventory(this, cells);
	}
	
	public void resize(int rows) {
		int cells = rows * ROW_WIDTH;
		Inventory inv2 = makeInv(cells, inv.getName());
		
		inv2.setContents(Arrays.copyOf(inv.getContents(), cells));
		handlers = Arrays.copyOf(handlers, cells);
		
		for(HumanEntity e : inv.getViewers()) {
			e.closeInventory();
			e.openInventory(inv2);
		}
		inv = inv2;
	}
	
	public void remove(int slot) {
		inv.clear(slot);
		handlers[slot] = null;
	}
	
	public void put(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
		if(slot >= inv.getSize())
			resize(slot / ROW_WIDTH + 1);
		if(item == null)
			inv.clear(slot);
		else
			// Arbitrary data to make double-click not pick up items from upper inv
			inv.setItem(slot, new ItemBuilder(item).flag(ItemFlag.HIDE_PLACED_ON).get());
		handlers[slot] = handler;
	}
	
	public Consumer<InventoryClickEvent> getHandler(int slot) {
		if(slot >= inv.getSize())
			return null;
		
		return handlers[slot];
	}
	
	public ItemStack getItem(int slot) {
		if(slot >= inv.getSize())
			return null;
	
		return inv.getItem(slot);
	}

	public void openFor(HumanEntity... viewers) {
		for(HumanEntity v : viewers)
			v.openInventory(inv);
	}
	
	public boolean requestCancel(Inventory inv, int slot) {
		return slot < inv.getSize();
	}
	
	public boolean click(InventoryClickEvent event) {
		// Ignore creative and clicking out of the inv
		if(event.getClick().isCreativeAction() || event.getRawSlot() < 0)
			return false;
		else if(event.getRawSlot() < event.getInventory().getSize()) {
			Consumer<InventoryClickEvent> handler = handlers[event.getSlot()];
			if(handler != null)
				handler.accept(event);
			
			return true;
		}
		// Allow non-transfer clicks
		return event.getClick().isShiftClick();
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
}
