package com.questworld;

import org.bukkit.inventory.ItemStack;

import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.util.ItemBuilder;

public class GuideBook {
	private static GuideBook instance = null;

	private final ItemStack guide;

	public static GuideBook instance() {
		if (instance == null)
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
		item = new ItemBuilder(item).wrapText("", "").get();
		ItemStack compare = new ItemBuilder(instance.guide).wrapText("", "").get();
		
		return ItemBuilder.compareItems(item, compare);
	}

	private GuideBook() {
		guide = new ItemBuilder(QuestWorld.getIcons().book_item)
				.wrapText(QuestWorld.translate(Translation.GUIDE_BOOK).split("\n")).get();
	}
}
