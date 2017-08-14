package me.mrCookieSlime.QuestWorld;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class GuideBook {
	private static GuideBook instance = null;
	private ItemStack guide;
	
	public static ItemStack get() {
		if(instance == null)
			instance = new GuideBook();
		
		return instance.guide.clone();
	}
	
	private GuideBook() {
		String display = QuestWorld.translate(Translation.book_display);
		String[] lore = QuestWorld.translate(Translation.book_lore).split("\n");
		
		guide = new ItemBuilder(Material.ENCHANTED_BOOK).display(display).lore(lore).get();
	}
}
