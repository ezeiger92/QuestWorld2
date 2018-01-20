package me.mrCookieSlime.QuestWorld.command;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.menu.PagedMapping;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player)sender;
			
			if (args.length == 0)
				QuestBook.openLastMenu(p);
			else {
				int c_id = -1;
				int q_id = -1;
				try {
					c_id = Integer.parseInt(args[0]);
				}
				catch(NumberFormatException exception) {
					sender.sendMessage(Text.colorize("&cError: invalid number for category (", args[0], ")"));
					return true;
				}
				if(args.length > 1) try {
					q_id = Integer.parseInt(args[1]);
				}
				catch(NumberFormatException exception) {
					sender.sendMessage(Text.colorize("&cError: invalid number for quest (", args[1], ")"));
					return true;
				}
				
				ICategory category = QuestWorld.getFacade().getCategory(c_id);
				if (category != null)  {
					if(QuestBook.testCategory(p, category)) {
						if (args.length == 2) {
							IQuest quest = category.getQuest(q_id);
							if(quest != null) {
								if(QuestBook.testQuest(p, quest)) {
									PagedMapping.clearPages(p);
									QuestBook.openQuest(p, quest, false, false);
								}
								else
									sender.sendMessage(Text.colorize("&cQuest unavailable"));
							}
							else
								sender.sendMessage(Text.colorize("&cMissing quest for index ", args[1], " (in category ", args[0], ")"));
						}
						else {
							PagedMapping.clearPages(p);
							QuestBook.openCategory(p, category, false);
						}
					}
					else
						sender.sendMessage(Text.colorize("&cCategory unavailable"));
				}
				else
					sender.sendMessage(Text.colorize("&cMissing category for index ", args[0]));
			}
		}
		else
			sender.sendMessage(Text.colorize("&4You are not a Player"));
		
		return true;
	}

}
