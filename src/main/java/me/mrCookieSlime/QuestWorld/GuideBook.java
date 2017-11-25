package me.mrCookieSlime.QuestWorld;

import java.util.HashSet;

import org.bukkit.Material;
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
	//private NamespacedKey key = new NamespacedKey(QuestWorld.getPlugin(), "GuideBook");
	
	public static ItemStack get() {
		if(instance == null)
			instance = new GuideBook();
		
		return instance.guide.clone();
	}
	
	public static void reset() {
		instance = null;
	}
	
	public static boolean isGuide(ItemStack item) {
		return item != null && pastBooks.contains(item.hashCode());
	}
	
	@SuppressWarnings("deprecation")
	public static ShapelessRecipe recipe() {
		 return new ShapelessRecipe(/*instance.key(1.12+),*/ get())
				 .addIngredient(Material.WORKBENCH);
	}
	
	private GuideBook() {
		String[] lines = QuestWorld.translate(Translation.GUIDE_BOOK).split("\n");
		//String display = QuestWorld.translate(Translation.BOOK_DISPLAY);
		//String[] lore = QuestWorld.translate(Translation.BOOK_LORE).split("\n");
		
		//new ItemBuilder(Material.ENCHANTED_BOOK).wrapText(display, lore);
		guide = new ItemBuilder(Material.ENCHANTED_BOOK).wrapText(lines).get();
		pastBooks.add(guide.hashCode());
	}
}
