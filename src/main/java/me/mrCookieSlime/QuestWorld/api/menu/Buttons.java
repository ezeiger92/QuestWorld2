package me.mrCookieSlime.QuestWorld.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;

public class Buttons {
	public static MenuClickHandler onCategory(Category category) {
		return new MenuClickHandler() {
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				if(action.isRightClicked())
					QBDialogue.openDeletionConfirmation(p, category);
				else if(action.isShiftClicked())
					QuestBook.openCategoryQuestEditor(p, category);
				else
					QuestBook.openCategoryEditor(p, category);
				return false;
			}
		};
	}
	
	public static MenuClickHandler newCategory(int id) {
		return new MenuClickHandler() {
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				String defaultCategoryName = QuestWorld.translate(Translation.default_category);
				PlayerTools.sendTranslation(p, true, Translation.category_namechange, defaultCategoryName);
				QuestWorld.getInstance().storeInput(p.getUniqueId(), new Input(InputType.CATEGORY_CREATION, id));
				p.closeInventory();
				return false;
			}
		};
	}
}
