package com.questworld;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.util.ItemBuilder;
import com.questworld.util.Reflect;

public class GuideBook {
	private static HashSet<Integer> pastBooks = new HashSet<>();
	private static volatile GuideBook instance = null;
	
	private final ItemStack guide;
	private final ShapelessRecipe recipe;
	
	public static GuideBook instance() {
		if(instance == null)
			instance = new GuideBook();
		
		return instance;
	}
	
	public ItemStack item() {
		return guide.clone();
	}
	
	public static void reset() {
		instance = null;
	}
	
	public static boolean isGuide(ItemStack item) {
		return item != null && pastBooks.contains(item.hashCode());
	}
	
	public ShapelessRecipe recipe() {
		 return recipe;
	}
	
	private GuideBook() {
		guide = new ItemBuilder(Material.ENCHANTED_BOOK).wrapText(
				QuestWorld.translate(Translation.GUIDE_BOOK).split("\n")).get();

		ShapelessRecipe r = null;

		if(!QuestWorld.getPlugin().getConfig().getBoolean("book.disable-recipe", false)) {
			r = Reflect.getAdapter().shapelessRecipe("GuideBook", guide).addIngredient(Material.WORKBENCH);
		}
			/*try {
				Class.forName("org.bukkit.NamespacedKey");
				r = new ShapelessRecipe(new NamespacedKey(QuestWorld.getPlugin(), "GuideBook"), guide)
						 .addIngredient(Material.WORKBENCH);
			}
			catch(ClassNotFoundException e) {
				Log.warning("Cannot register recipe: Are you using an old server version?");
				e.printStackTrace();
			}*/
		
		recipe = r;
		
		pastBooks.add(guide.hashCode());
	}
}
