package me.mrCookieSlime.QuestWorld;

import org.bukkit.Material;
//import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;

public class GuideBook {
	private static GuideBook instance = null;
	private ItemStack guide;
	//private NamespacedKey key = new NamespacedKey(QuestWorld.getInstance(), "GuideBook");
	
	public static ItemStack get() {
		if(instance == null)
			instance = new GuideBook();
		
		return instance.guide.clone();
	}
	
	@SuppressWarnings("deprecation")
	public static ShapelessRecipe recipe() {
		 return new ShapelessRecipe(/*instance.key(1.12+),*/ get())
				 .addIngredient(Material.WORKBENCH);
	}
	
	private GuideBook() {
		String display = QuestWorld.translate(Translation.BOOK_DISPLAY);
		String[] lore = QuestWorld.translate(Translation.BOOK_LORE).split("\n");
		
		guide = new ItemBuilder(Material.ENCHANTED_BOOK).display(display).lore(lore).get();
	}
}
