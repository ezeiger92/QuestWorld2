package me.mrCookieSlime.QuestWorld.command;


import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.api.menu.QBDialogue;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.util.Log;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditorCommand implements CommandExecutor {
	
	private void help(String label, CommandSender sender) {
		sender.sendMessage(Text.colorize("&4Usage: &c/", label, " <gui/save/upgrade/import <File>/export <File>/reload <config/quests/all>"));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			help(label, sender);
			return true;
		}
		Player p;
		if(sender instanceof Player)
			p = (Player)sender;
		else
			p = null;
		
		String param = args[0].toLowerCase();
		if (param.equals("import")) {
			if(args.length < 2) {
				sender.sendMessage(Text.colorize("&cMissing argument: /", label, " import &6<File>"));
				return true;
			}
			args[1] += ".zip";
			if(QuestWorld.get().importPreset(args[1]))
				sender.sendMessage(Text.colorize("&7Successfully installed the Preset &a", args[1]));
			else
				sender.sendMessage(Text.colorize("&cThe Preset &4", args[1], " &ccould not be installed"));
		}
		else if (param.equals("export")) {
			if(args.length < 2) {
				sender.sendMessage(Text.colorize("&cMissing argument: /", label, " export &6<File>"));
				return true;
			}
			args[1] += ".zip";
			if(QuestWorld.get().exportPreset(args[1]))
				sender.sendMessage(Text.colorize("&7Successfully saved the Preset &a", args[1]));
			else
				sender.sendMessage(Text.colorize("&cCould not save Preset &a", args[1]));
		}
		else if (param.equals("gui")) {
			if (sender instanceof Player) {
				PlayerManager.of((Player)sender).clearPages();
				QuestBook.openEditor((Player) sender);
			}
			else
				sender.sendMessage(Text.colorize("&4You are not a Player"));
		}
		else if(param.equals("save")) {
			// Command, force save, this is probably desired over an incremental save
			QuestWorld.get().save(true);
			sender.sendMessage(Text.colorize("&7Saved all quests to disk"));
		}
		else if(param.equals("reload")) {
			if(args.length > 1 && args[1].equalsIgnoreCase("config")) {
				QuestWorld.get().reloadQWConfig();
				sender.sendMessage(Text.colorize("&7Reloaded config from disk"));
			}
			else if(args.length > 1 && args[1].equalsIgnoreCase("quests")) {
				QuestWorld.get().reloadQuests();
				sender.sendMessage(Text.colorize("&7Reloaded all quests from disk"));
			}
			else if(args.length > 1 && args[1].equalsIgnoreCase("all")) {
				QuestWorld.get().reloadQWConfig();
				QuestWorld.get().reloadQuests();
				sender.sendMessage(Text.colorize("&7Reloaded config and all quests from disk"));
			}
			else {
				sender.sendMessage(Text.colorize("&7/", label, " reload config - &fReload config files"));
				sender.sendMessage(Text.colorize("&7/", label, " reload quests - &fReload quest files"));
				sender.sendMessage(Text.colorize("&7/", label, " reload all - &fReload config and quest files"));
			}
		}
		else if(param.equals("upgrade")) {
			if(args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
				int changeCount = 0;
				for(ICategory category : QuestWorld.get().getCategories())
					for(IQuest quest : category.getQuests())
						if(quest.getCooldown() == 0) {
							// Administrative process - bypass events and directly modify quest
							// 99% of the time you should use .getState() and .apply()
							IQuestState q = (IQuestState)quest;
							q.setCooldown(-1);
							++changeCount;
							String questFile = quest.getID() + "-C" + category.getID();
							Log.info("[Quest World 2] Upgrading "+category.getName()+"."+quest.getName()+" ("+questFile+".quest): Cooldown changed from 0 to -1");
						}
				
				String s = "s";
				if(changeCount == 1)
					s = "";
				
				String message = "&7Upgrade complete, "+changeCount+" quest"+s+" were modified";
				if(changeCount > 0)
					message += ", changes printed in console";
				
				sender.sendMessage(Text.colorize(message));
				return true;
			}
			sender.sendMessage(Text.colorize("&cWarning! this will change all quests with 0 cooldown to -1 cooldown to match new behavior"));
			sender.sendMessage(Text.colorize("&cIf you've made any 0 cooldown quests in 2.6.3 or later, they will be affected to!"));
			sender.sendMessage(Text.colorize("  &7If you wish to continue, type /", label, " upgrade confirm"));
			
			return true;
		}
		else if (args.length == 4 && param.equals("delete_command") && sender instanceof Player) {
			IQuest quest = QuestWorld.get().getCategory(Integer.parseInt(args[1])).getQuest(Integer.parseInt(args[2]));
			
			IQuestState changes = quest.getState();
			changes.removeCommand(Integer.parseInt(args[3]));
			if(changes.apply()) {
				
			}
			
			QBDialogue.openCommandEditor((Player) sender, quest);
		}
		else if (args.length == 3 && param.equals("add_command") && sender instanceof Player) {
			IQuest quest = QuestWorld.get().getCategory(Integer.parseInt(args[1])).getQuest(Integer.parseInt(args[2]));
			//sender.sendMessage(Text.colorize("&7Type in your desired Command:"));
			//QuestWorld.getInstance().storeInput(((Player) sender).getUniqueId(), new Input(InputType.COMMAND_ADD, quest));
			
			PlayerTools.promptInput(p, new SinglePrompt(
					"&7Type in your desired Command:",
					(c,s) -> {
						IQuestState changes = quest.getState();
						changes.addCommand(ChatColor.stripColor(s));
						changes.apply();

						QBDialogue.openCommandEditor(p, quest);
						return true;
					}
			));
			sender.sendMessage(Text.colorize("&7Usable Variables: @p (Username)"));
			
		}
		else {
			help(label, sender);
		}
		return true;
	}

}
