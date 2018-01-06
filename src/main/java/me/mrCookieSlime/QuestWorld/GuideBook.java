package me.mrCookieSlime.QuestWorld;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
//import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;

public class GuideBook {
	private static HashSet<Integer> pastBooks = new HashSet<>();
	private static GuideBook instance = null;
	private ItemStack guide;
	private NamespacedKey key = new NamespacedKey(QuestWorld.getPlugin(), "GuideBook");
	
	private static GuideBook instance() {
		if(instance == null)
			instance = new GuideBook();
		
		return instance;
	}
	
	public static ItemStack get() {
		return instance().guide.clone();
	}
	
	public static void reset() {
		instance = null;
	}
	
	public static boolean isGuide(ItemStack item) {
		return item != null && pastBooks.contains(item.hashCode());
	}
	
	public static ShapelessRecipe recipe() {
		 return new ShapelessRecipe(instance().key, get())
				 .addIngredient(Material.WORKBENCH);
	}
	
	private GuideBook() {
		guide = new ItemBuilder(Material.ENCHANTED_BOOK).wrapText(
				QuestWorld.translate(Translation.GUIDE_BOOK).split("\n")).get();
		pastBooks.add(guide.hashCode());
	}
}
