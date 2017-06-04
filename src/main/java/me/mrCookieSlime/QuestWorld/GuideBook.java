package me.mrCookieSlime.QuestWorld;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class GuideBook {
	private static GuideBook instance = null;
	private ItemStack guide;
	
	public static ItemStack get() {
		if(instance == null)
			instance = new GuideBook();
		
		return instance.guide.clone();
	}
	
	public static boolean isSimilar(ItemStack stack) {
		if(instance == null)
			instance = new GuideBook();
		
		return stack.isSimilar(instance.guide);
	}
	
	private GuideBook() {
		guide = new ItemBuilder(Material.ENCHANTED_BOOK)
			.display("&eQuest Book &7(Right Click)")
			.lore(
				"",
				"&rYour basic Guide for Quests",
				"&rIn case you lose it, simply place a",
				"&rWorkbench into your Crafting Grid")
			.get();
	}
}
