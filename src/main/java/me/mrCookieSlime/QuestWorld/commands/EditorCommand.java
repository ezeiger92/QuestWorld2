package me.mrCookieSlime.QuestWorld.commands;


import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.Log;
import me.mrCookieSlime.QuestWorld.utils.Text;

import java.util.Iterator;

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
		
		if(!sender.hasPermission("QuestWorld.editor")) {
			sender.sendMessage(Text.colorize("&cYou are not allowed to do this"));
			return true;
		}
		
		if(args.length == 0) {
			help(label, sender);
			return true;
		}
		
		String param = args[0].toLowerCase();
		if (param.equals("import")) {
			if(args.length < 2) {
				sender.sendMessage(Text.colorize("&cMissing argument: /", label, " import &6<File>"));
				return true;
			}
			args[1] += ".zip";
			if(QuestWorld.getInstance().importPreset(args[1]))
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
			if(QuestWorld.getInstance().exportPreset(args[1]))
				sender.sendMessage(Text.colorize("&7Successfully saved the Preset &a", args[1]));
			else
				sender.sendMessage(Text.colorize("&cCould not save Preset &a", args[1]));
		}
		else if (param.equals("gui")) {
			if (sender instanceof Player)
				QuestBook.openEditor((Player) sender);
			else
				sender.sendMessage(Text.colorize("&4You are not a Player"));
		}
		else if(param.equals("save")) {
			// Command, force save, this is probably desired over an incremental save
			QuestWorld.getInstance().save(true);
			sender.sendMessage(Text.colorize("&7Saved all quests to disk"));
		}
		else if(param.equals("reload")) {
			if(args.length > 1 && args[1].equalsIgnoreCase("config")) {
				QuestWorld.getInstance().reloadQWConfig();
				sender.sendMessage(Text.colorize("&7Reloaded config from disk"));
			}
			else if(args.length > 1 && args[1].equalsIgnoreCase("quests")) {
				QuestWorld.getInstance().reloadQuests();
				sender.sendMessage(Text.colorize("&7Reloaded all quests from disk"));
			}
			else if(args.length > 1 && args[1].equalsIgnoreCase("all")) {
				QuestWorld.getInstance().reloadQWConfig();
				QuestWorld.getInstance().reloadQuests();
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
				int changes = 0;
				Iterator<Category> categories = QuestWorld.getInstance().getCategories().iterator();
				while(categories.hasNext()) {
					Category c = categories.next();
					Iterator<Quest> quests = c.getQuests().iterator();
					while(quests.hasNext()) {
						Quest q = quests.next();
						if(q.getCooldown() == 0) {
							q.setCooldown(-1);
							++changes;
							String questFile = q.getID() + "-C" + c.getID();
							Log.info("[Quest World 2] Upgrading "+c.getName()+"."+q.getName()+" ("+questFile+".quest): Cooldown changed from 0 to -1");
						}
					}
				}
				String s = "s";
				if(changes == 1)
					s = "";
				
				String message = "&7Upgrade complete, "+changes+" quest"+s+" were modified";
				if(changes > 0)
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
			Quest quest = QuestWorld.getInstance().getCategory(Integer.parseInt(args[1])).getQuest(Integer.parseInt(args[2]));
			quest.removeCommand(Integer.parseInt(args[3]));
			QBDialogue.openCommandEditor((Player) sender, quest);
		}
		else if (args.length == 3 && param.equals("add_command") && sender instanceof Player) {
			Quest quest = QuestWorld.getInstance().getCategory(Integer.parseInt(args[1])).getQuest(Integer.parseInt(args[2]));
			sender.sendMessage(Text.colorize("&7Type in your desired Command:"));
			sender.sendMessage(Text.colorize("&7Usable Variables: @p (Username)"));
			QuestWorld.getInstance().storeInput(((Player) sender).getUniqueId(), new Input(InputType.COMMAND_ADD, quest));
		}
		else {
			help(label, sender);
		}
		return true;
	}

}
