package com.questworld.command;

import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IQuest;
import com.questworld.api.menu.PagedMapping;
import com.questworld.api.menu.QuestBook;
import com.questworld.util.Text;

public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {

			Player p = (Player) sender;
			ICategory category = null;
			IQuest quest = null;
			int page = -1;

			int index = 0;

			if (args.length > index) {
				try {
					category = QuestWorld.getFacade().getCategory(Integer.parseInt(args[index]));
				}
				catch (NumberFormatException e) {
				}

				if (category != null) {
					++index;

					if (args.length > index) {
						try {
							quest = category.getQuest(Integer.parseInt(args[index]));
						}
						catch (NumberFormatException e) {
						}

						if (quest != null)
							++index;
					}
				}
			}

			if (args.length > index) {
				String tail = args[index].toLowerCase(Locale.US);

				if (tail.equals("page")) {
					if (args.length > index + 1) {
						try {
							page = Integer.parseInt(args[index + 1]) - 1;
						}
						catch (NumberFormatException e) {
						}
					}

					if (page < 0) {
						// error
						return true;
					}
				}
				else {
					// error
					return true;
				}
			}

			open(p, category, quest, page, false);
		}
		else
			sender.sendMessage(Text.colorize(QuestWorld.translate(Translation.NOT_PLAYER)));

		return true;
	}

	public static void open(Player p, ICategory category, IQuest quest, int page, boolean force) {
		if (category != null) {
			if (force || QuestBook.testCategory(p, category)) {
				if (quest != null) {
					if (force || QuestBook.testQuest(p, quest)) {
						QuestBook.openQuest(p, quest, false, false);
					}
					else
						p.sendMessage(Text.colorize(QuestWorld.translate(Translation.QUEST_UNAVAIL)));
				}
				else {
					PagedMapping.clearPages(p);
					PagedMapping.putPage(p, category.getID() / 45);
					PagedMapping.putPage(p, page);
					QuestBook.openCategory(p, category, true);
				}
			}
			else
				p.sendMessage(Text.colorize(QuestWorld.translate(Translation.CAT_UNAVAIL)));
		}
		else {
			if (page >= 0) {
				PagedMapping.clearPages(p);
				PagedMapping.putPage(p, page);
				QuestBook.openMainMenu(p);
			}
			else
				QuestBook.openLastMenu(p);
		}
	}
}
