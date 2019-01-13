package com.questworld.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

// TODO: Unused
public abstract class Reward {
	public abstract boolean apply(Player player);
	public abstract ItemStack getDisplay();
	public abstract void configure(InventoryClickEvent click);
}
