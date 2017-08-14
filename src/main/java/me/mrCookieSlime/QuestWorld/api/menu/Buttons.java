package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.AdvancedMenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;

public class Buttons {
	public static MenuClickHandler onCategory(Category category) {
		return new MenuClickHandler() {
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				if(action.isRightClicked())
					QBDialogue.openDeletionConfirmation(p, category);
				else if(action.isShiftClicked()) {
					QuestBook.openCategoryEditor(p, category);
				}
				else {
					QuestWorld.getInstance().getManager(p).putPage(0);
					QuestBook.openCategoryQuestEditor(p, category);
				}
				return false;
			}
		};
	}
	
	public static MenuClickHandler newCategory(int id) {
		return new MenuClickHandler() {
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
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
				
				//PlayerTools.sendTranslation(p, true, Translation.category_namechange, defaultCategoryName);
				//QuestWorld.getInstance().storeInput(p.getUniqueId(), new Input(InputType.CATEGORY_CREATION, id));
				p.closeInventory();
				return false;
			}
		};
	}
	
	public static MenuClickHandler onQuest(Quest quest) {
		return new MenuClickHandler() {
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				if(action.isRightClicked())
					QBDialogue.openDeletionConfirmation(p, quest);
				else if(action.isShiftClicked()) {
					QuestBook.openQuestEditor(p, quest);
				}
				else {
					// TODO Open missions 
					QuestBook.openQuestEditor(p, quest);
				}
				return false;
			}
		};
	}
	
	public static MenuClickHandler newQuest(int cat_id, int id) {
		return new MenuClickHandler() {
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				String defaultQuestName = QuestWorld.translate(Translation.default_quest);
				//PlayerTools.sendTranslation(p, true, Translation.quest_namechange, defaultQuestName);
				//QuestWorld.getInstance().storeInput(p.getUniqueId(), new Input(InputType.QUEST_CREATION, cat_id + " M " + id));
				
				PlayerTools.promptInput(p, new SinglePrompt(
						PlayerTools.makeTranslation(true, Translation.quest_namechange, defaultQuestName),
						(c,s) -> {
							new Quest(s, cat_id+" M "+id);
							PlayerTools.sendTranslation(p, true, Translation.quest_created, s);
							QuestBook.openCategoryQuestEditor(p, QuestWorld.getInstance().getCategory(cat_id));

							return true;
						}
				));
				
				p.closeInventory();
				return false;
			}
		};
	}
	
	public static MenuClickHandler simpleHandler(Consumer<InventoryClickEvent> action) {
		return new AdvancedMenuClickHandler() {
			// Unused in advanced click
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) { return true; }

			@Override
			public boolean onClick(InventoryClickEvent event, Player p, int arg2, ItemStack arg3, ClickAction arg4) {
				action.accept(event);
				return false;
			}
			
		};
	}
}
