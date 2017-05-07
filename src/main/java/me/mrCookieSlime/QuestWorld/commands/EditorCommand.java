package me.mrCookieSlime.QuestWorld.commands;


import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditorCommand implements CommandExecutor {
	
	private void help(String label, CommandSender sender) {
		sender.sendMessage(Text.colorize("&4Usage: &c/", label, " <gui/import <File> /export <File> >"));
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
			QuestWorld.getInstance().save();
			sender.sendMessage(Text.colorize("&7Saved all quests to disk"));
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
