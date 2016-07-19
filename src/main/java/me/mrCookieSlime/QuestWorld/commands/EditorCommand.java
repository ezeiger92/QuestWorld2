package me.mrCookieSlime.QuestWorld.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class EditorCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender || sender.hasPermission("QuestWorld.editor")) {
			if (args.length == 2 && args[0].equalsIgnoreCase("import")) {
				File file = new File("plugins/QuestWorld/presets/" + args[1] + ".zip");
				byte[] buffer = new byte[1024];
				if (file.exists()) {
					QuestWorld.getInstance().unload();
					try {
						ZipInputStream input = new ZipInputStream(new FileInputStream(file));
						ZipEntry entry = input.getNextEntry();
						
						for (File f: new File("plugins/QuestWorld/quests").listFiles()) {
							f.delete();
						}
						
						while (entry != null) {
							FileOutputStream output = new FileOutputStream(new File("plugins/QuestWorld/quests/" + entry.getName()));
							
							int length;
							while ((length = input.read(buffer)) > 0) {
								output.write(buffer, 0, length);
							}
							
							output.close();
							entry = input.getNextEntry();
						}
						
						input.closeEntry();
						input.close();

						QuestWorld.getInstance().load();
						sender.sendMessage("§7Successfully installed the Preset §a" + args[1] + ".zip");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else sender.sendMessage("§cThe Preset §4" + args[1] + ".zip §ccould not be found");
			}
			else if (args.length == 2 && args[0].equalsIgnoreCase("export")) {
				File file = new File("plugins/QuestWorld/presets/" + args[1] + ".zip");
				byte[] buffer = new byte[1024];
				
				if (file.exists()) file.delete();
				
				try {
					QuestWorld.getInstance().unload();
					QuestWorld.getInstance().load();
					file.createNewFile();
					
					ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file));
					for (File f: new File("plugins/QuestWorld/quests").listFiles()) {
						ZipEntry entry = new ZipEntry(f.getName());
						output.putNextEntry(entry);
						FileInputStream input = new FileInputStream(f);
						
						int length;
						while ((length = input.read(buffer)) > 0) {
							output.write(buffer, 0, length);
						}
						
						input.close();
						output.closeEntry();
					}
					output.close();
					sender.sendMessage("§7Successfully saved the Preset §a" + args[1] + ".zip");
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			else if (args.length == 1 && args[0].equalsIgnoreCase("gui")) {
				if (sender instanceof Player) {
					QuestBook.openEditor((Player) sender);
				}
				else sender.sendMessage("§4You are not a Player");
			}
			else if (args.length == 4 && args[0].equalsIgnoreCase("delete_command") && sender instanceof Player) {
				Quest quest = QuestWorld.getInstance().getCategory(Integer.parseInt(args[1])).getQuest(Integer.parseInt(args[2]));
				quest.removeCommand(Integer.parseInt(args[3]));
				QBDialogue.openCommandEditor((Player) sender, quest);
			}
			else if (args.length == 3 && args[0].equalsIgnoreCase("add_command") && sender instanceof Player) {
				Quest quest = QuestWorld.getInstance().getCategory(Integer.parseInt(args[1])).getQuest(Integer.parseInt(args[2]));
				sender.sendMessage("§7Type in your desired Command:");
				sender.sendMessage("§7Usable Variables: @p (Username)");
				QuestWorld.getInstance().storeInput(((Player) sender).getUniqueId(), new Input(InputType.COMMAND_ADD, quest));
			}
			else {
				sender.sendMessage("§4Usage: §c/questeditor <gui/import <File> /export <File> >");
			}
		}
		else sender.sendMessage("§4You are not allowed to do this");
		return true;
	}

}
