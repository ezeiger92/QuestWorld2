package me.mrCookieSlime.QuestWorld.command;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.menu.PagedMapping;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.util.Text;

import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			
			Player p = (Player)sender;
			ICategory category = null;
			IQuest quest = null;
			int page = -1;
			
			int index = 0;
			
			if(args.length > index) {
				try {
					category = QuestWorld.getFacade().getCategory(Integer.parseInt(args[index]));
				}
				catch(NumberFormatException e) {
				}
				
				if(category != null) {
					++index;
					
					if(args.length > index) {
						try {
							quest = category.getQuest(Integer.parseInt(args[index]));
						}
						catch(NumberFormatException e) {
						}
						
						if(quest != null)
							++index;
					}
				}
			}
			
			if(args.length > index) {
				String tail = args[index].toLowerCase(Locale.US);
				
				if(tail.equals("page")) {
					if(args.length > index + 1) {
						try {
							page = Integer.parseInt(args[index + 1]) - 1;
						}
						catch(NumberFormatException e) {
						}
					}
					
					if(page < 0) {
						//error
						return true;
					}
				}
				else {
					//error
					return true;
				}
			}
			
			if(category != null) {
				if(QuestBook.testCategory(p, category)) {
					if(quest != null) {
						if(QuestBook.testQuest(p, quest)) {
							QuestBook.openQuest(p, quest, false, false);
						}
						else
							sender.sendMessage(Text.colorize("&cQuest unavailable"));
					}
					else {
						PagedMapping.clearPages(p);
						PagedMapping.putPage(p, category.getID() / 45);
						PagedMapping.putPage(p, page);
						QuestBook.openCategory(p, category, true);
					}
				}
				else
					sender.sendMessage(Text.colorize("&cCategory unavailable"));
			}
			else {
				if(page >= 0) {
					PagedMapping.clearPages(p);
					PagedMapping.putPage(p, page);
					QuestBook.openMainMenu(p);
				}
				else
					QuestBook.openLastMenu(p);
			}
		}
		else
			sender.sendMessage(Text.colorize("&4You are not a Player"));
		
		return true;
	}

}
