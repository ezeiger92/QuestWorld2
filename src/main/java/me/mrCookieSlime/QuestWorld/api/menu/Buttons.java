package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;

public class Buttons {
	public static Consumer<InventoryClickEvent> onCategory(Category category) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			if(event.isRightClick())
				QBDialogue.openDeletionConfirmation(p, category);
			else if(event.isShiftClick())
				QuestBook.openCategoryEditor(p, category);
			else {
				QuestWorld.getInstance().getManager(p).putPage(0);
				QuestBook.openCategoryQuestEditor(p, category);
			}
		};
	}
	
	public static Consumer<InventoryClickEvent> newCategory(int id) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			String defaultCategoryName = QuestWorld.translate(Translation.default_category);
			
			PlayerTools.promptInput(p, new SinglePrompt(
					PlayerTools.makeTranslation(true, Translation.category_namechange, defaultCategoryName),
					(c,s) -> {
						new Category(s, id);
						PlayerTools.sendTranslation(p, true, Translation.category_created, s);
						QuestBook.openEditor(p);

						return true;
					}
			));
			
			PlayerTools.closeInventoryWithEvent(p);
		};
	}
	
	public static Consumer<InventoryClickEvent> onQuest(Quest quest) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			if(event.isRightClick())
				QBDialogue.openDeletionConfirmation(p, quest);
			else if(event.isShiftClick()) {
				QuestBook.openQuestEditor(p, quest);
			}
			else {
				// TODO Open missions 
				QuestBook.openQuestEditor(p, quest);
			}
		};
	}
	
	public static Consumer<InventoryClickEvent> newQuest(int cat_id, int id) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			String defaultQuestName = QuestWorld.translate(Translation.default_quest);
			
			PlayerTools.promptInput(p, new SinglePrompt(
					PlayerTools.makeTranslation(true, Translation.quest_namechange, defaultQuestName),
					(c,s) -> {
						new Quest(s, cat_id+" M "+id);
						PlayerTools.sendTranslation(p, true, Translation.quest_created, s);
						QuestBook.openCategoryQuestEditor(p, QuestWorld.getInstance().getCategory(cat_id));

						return true;
					}
			));

			PlayerTools.closeInventoryWithEvent(p);
		};
	}
}
