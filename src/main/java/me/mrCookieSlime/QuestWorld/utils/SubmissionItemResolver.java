package me.mrCookieSlime.QuestWorld.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;

public class SubmissionItemResolver {

	public static ItemStack mobEgg(EntityType entity) {
		return new ItemBuilder(Material.MONSTER_EGG).mob(entity).display("&7Entity Type: &r" + Text.niceName(entity.name())).get();
	}
	
	public static ItemStack location(Material mat, Location location) {
		return new CustomItem(mat, "&7X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ(), 0);
	}
}
