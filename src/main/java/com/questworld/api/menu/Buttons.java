package com.questworld.api.menu;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.questworld.api.QuestWorld;
import com.questworld.api.SinglePrompt;
import com.questworld.api.Translation;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.ICategoryState;
import com.questworld.api.contract.IQuest;
import com.questworld.api.lang.CategoryReplacements;
import com.questworld.api.lang.CustomReplacements;
import com.questworld.api.lang.QuestReplacements;
import com.questworld.util.PlayerTools;
import com.questworld.util.Text;

public class Buttons {
	public static Consumer<InventoryClickEvent> onCategory(ICategory category) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			if (event.isRightClick())
				QBDialogue.openDeletionConfirmation(p, category);
			else if (event.isShiftClick())
				QuestBook.openCategoryEditor(p, category);
			else {
				PagedMapping.putPage(p, 0);
				QuestBook.openQuestList(p, category);
			}
		};
	}

	public static Consumer<InventoryClickEvent> newCategory(int id) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			String defaultCategoryName = QuestWorld.translate(p, Translation.DEFAULT_CATEGORY);

			PlayerTools.promptInput(p, new SinglePrompt(
					PlayerTools.makeTranslation(true, Translation.CATEGORY_NAME_EDIT, new CustomReplacements().Add("name", defaultCategoryName)), (c, s) -> {
						s = Text.deserializeNewline(Text.colorize(s));
						ICategory category = QuestWorld.getFacade().createCategory(s, id);
						PlayerTools.sendTranslation(p, true, Translation.CATEGORY_CREATED, new CategoryReplacements(category));
						QuestBook.openCategoryList(p);

						return true;
					}));
		};
	}

	public static Consumer<InventoryClickEvent> onQuest(IQuest quest) {
		return event -> {
			Player p = (Player) event.getWhoClicked();
			if (event.isRightClick())
				QBDialogue.openDeletionConfirmation(p, quest);
			else if (event.isShiftClick()) {
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
			String defaultQuestName = QuestWorld.translate(p, Translation.DEFAULT_QUEST);

			PlayerTools.promptInput(p, new SinglePrompt(
					PlayerTools.makeTranslation(true, Translation.QUEST_NAME_EDIT, new CustomReplacements().Add("name", defaultQuestName)), (c, s) -> {
						ICategoryState state = category.getState();
						s = Text.deserializeNewline(Text.colorize(s));
						IQuest quest = state.addQuest(s, id);
						if (state.apply()) {
							PlayerTools.sendTranslation(p, true, Translation.QUEST_CREATED, new QuestReplacements(quest));
						}

						QuestBook.openQuestList(p, category);

						return true;
					}));
		};
	}

	public static Consumer<InventoryClickEvent> partyMenu() {
		return event -> {
			if (QuestWorld.getPlugin().getConfig().getBoolean("party.enabled")) {
				Player p = (Player) event.getWhoClicked();

				QuestBook.openPartyMenu(p);
			}
		};
	}
}
