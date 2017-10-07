package me.mrCookieSlime.QuestWorld.command;

import java.util.UUID;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.party.Party;
import me.mrCookieSlime.QuestWorld.quest.Category;
import me.mrCookieSlime.QuestWorld.quest.QuestBook;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player)sender;
			if (args.length == 0) QuestBook.openLastMenu(p);
			else {
				if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
					Party party = QuestWorld.getInstance().getManager(Bukkit.getOfflinePlayer(UUID.fromString(args[1]))).getParty();
					if (party != null && party.hasInvited(p)) {
						int maxParty = QuestWorld.getInstance().getCfg().getInt("party.max-members");
						if (party.getSize() >= maxParty) {
							PlayerTools.sendTranslation(sender, true, Translation.PARTY_ERROR_FULL, Integer.toString(maxParty));
						}
						else party.playerJoin(p);
					}
				}
				else {
					try {
						Category category = QuestWorld.getInstance().getCategory(Integer.parseInt(args[0]));
						if (category != null)  {
							QuestWorld.getInstance().getManager((Player)sender).clearPages();
							if (args.length == 2) QuestBook.openQuest((Player) sender, category.getQuest(Integer.parseInt(args[1])), false, false);
							else QuestBook.openCategory((Player) sender, category, false);
						}
						else sender.sendMessage(Text.colorize("&4Unknown Category: &c", args[0]));
					} catch(Exception x) {
						 sender.sendMessage(Text.colorize("&4Unknown Category: &c", args[0]));
					}
				}
			}
		}
		else sender.sendMessage(Text.colorize("&4You are not a Player"));
		return true;
	}

}
