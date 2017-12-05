package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;

public class Buttons {
	public static Consumer<InventoryClickEvent> onCategory(ICategory category) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			if(event.isRightClick())
				QBDialogue.openDeletionConfirmation(p, category);
			else if(event.isShiftClick())
				QuestBook.openCategoryEditor(p, category);
			else {
				PagedMapping.putPage(p, 0);
				QuestBook.openCategoryQuestEditor(p, category);
			}
		};
	}
	
	public static Consumer<InventoryClickEvent> newCategory(int id) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			String defaultCategoryName = QuestWorld.translate(Translation.DEFAULT_CATEGORY);
			
			PlayerTools.promptInput(p, new SinglePrompt(
					PlayerTools.makeTranslation(true, Translation.CATEGORY_NAME_EDIT, defaultCategoryName),
					(c,s) -> {
						QuestWorld.getFacade().createCategory(s, id);
						PlayerTools.sendTranslation(p, true, Translation.CATEGORY_CREATED, s);
						QuestBook.openEditor(p);

						return true;
					}
			));
			
			PlayerTools.closeInventoryWithEvent(p);
		};
	}
	
	public static Consumer<InventoryClickEvent> onQuest(IQuest quest) {
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
	
	public static Consumer<InventoryClickEvent> newQuest(ICategory category, int id) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			String defaultQuestName = QuestWorld.translate(Translation.DEFAULT_QUEST);
			
			PlayerTools.promptInput(p, new SinglePrompt(
					PlayerTools.makeTranslation(true, Translation.QUEST_NAME_EDIT, defaultQuestName),
					(c,s) -> {
						ICategoryState state = category.getState();
						state.addQuest(s, id);
						if(state.apply()) {
							PlayerTools.sendTranslation(p, true, Translation.QUEST_CREATED, s);
						}
						
						QuestBook.openCategoryQuestEditor(p, category);

						return true;
					}
			));

			PlayerTools.closeInventoryWithEvent(p);
		};
	}
	
	public static Consumer<InventoryClickEvent> partyMenu() {
		return event -> {
			if (QuestWorld.getPlugin().getConfig().getBoolean("party.enabled")) {
				Player p = (Player) event.getWhoClicked();
				
				// TODO openPartyMenu has no way to go back to where it came from, so it always goes to the main menu
				// As a result, we need to clear pages to avoid odd behavior. RIP.
				PagedMapping.clearPages(p);
				QuestBook.openPartyMenu(p);
			}
		};
	}
}
