package me.mrCookieSlime.QuestWorld.command;

import me.mrCookieSlime.QuestWorld.GuideBook;
import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.api.menu.PagedMapping;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.manager.PlayerStatus;
import me.mrCookieSlime.QuestWorld.util.Log;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditorCommand implements CommandExecutor {
	
	private final QuestWorldPlugin plugin;
	public EditorCommand(QuestWorldPlugin plugin) {
		this.plugin = plugin;
	}
	
	private void help(String label, CommandSender sender) {
		String version = plugin.getDescription().getVersion();
		sender.sendMessage(Text.colorize("&3== &b/", label, " help &3: &bv"+version+" &3== "));
		sender.sendMessage(Text.colorize("  &bgui &7- Open the editor gui"));
		sender.sendMessage(Text.colorize("  &bbook [player] &7- Give yourself (or player) a questbook"));
		sender.sendMessage(Text.colorize("  &breload &7- Reloads config files from disk"));
		sender.sendMessage(Text.colorize("  &bsave &7- Save all in-game config and quest changes to disk"));
		sender.sendMessage(Text.colorize("  &bextension &7- View extensions"));
		sender.sendMessage(Text.colorize("  &bexport <file> &7- Save all quests to a preset"));
		sender.sendMessage(Text.colorize("  &bimport <file> &4&l*&7 - Overwrite all quests with a preset"));
		sender.sendMessage(Text.colorize("  &bdiscard &4&l*&7 - Reloads quest data from disk, losing changes"));
		sender.sendMessage(Text.colorize("  &bupgrade &4&l*&7 - Replaces all cooldowns of 0 with -1"));
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
		
		String param = args[0].toLowerCase(Locale.US);
		
		if (param.equals("import")) {
			if(args.length < 2) {
				sender.sendMessage(Text.colorize("&cMissing argument: /", label, " import &6<File>"));
				return true;
			}
			args[1] += ".zip";
			if(plugin.importPreset(args[1]))
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
			if(plugin.exportPreset(args[1]))
				sender.sendMessage(Text.colorize("&7Successfully saved the Preset &a", args[1]));
			else
				sender.sendMessage(Text.colorize("&cCould not save Preset &a", args[1]));
		}
		else if (param.equals("extension")) {
			ExtensionControl.func(sender, cmd, label, Arrays.copyOfRange(args, 1, args.length));
		}
		else if (param.equals("gui")) {
			if (p != null) {
				PagedMapping.clearPages(p);
				QuestBook.openCategoryList(p);
			}
			else
				sender.sendMessage(Text.colorize("&4You are not a Player"));
		}
		else if (param.equals("book")) {
			Player recipient = p;
			if(args.length > 1)
				recipient = PlayerTools.getPlayer(args[1]);
			
			if(recipient != null) {
				recipient.getInventory().addItem(GuideBook.instance().item());
				if(p != recipient)
					p.sendMessage(Text.colorize("&3Given QuestBook to &b" + recipient.getName()));
				recipient.sendMessage(Text.colorize("&3Recieved QuestBook"));
			}
			else
				p.sendMessage(Text.colorize("&cCould not find player &e" + args[1]));
		}
		else if(param.equals("discard")) {
			plugin.onDiscard();
			sender.sendMessage(Text.colorize("&7Reloaded all quests from disk"));
		}
		else if(param.equals("save")) {
			// Command, force save, this is probably desired over an incremental save
			plugin.onSave(true);
			sender.sendMessage(Text.colorize("&7Saved all quests to disk"));
		}
		else if(param.equals("reload")) {
			if(args.length > 1 && args[1].equalsIgnoreCase("quests")) {
				sender.sendMessage(Text.colorize("&cThis usage is deprecated! Use /qe discard instead"));
				plugin.onDiscard();
				sender.sendMessage(Text.colorize("&7Reloaded all quests from disk"));
			}
			else if(args.length > 1 && args[1].equalsIgnoreCase("all")) {
				sender.sendMessage(Text.colorize("&cThis usage is deprecated! Use /qe reload and /qe discard instead"));
				plugin.onReload();
				plugin.onDiscard();
				sender.sendMessage(Text.colorize("&7Reloaded config and all quests from disk"));
			}
			else {
				plugin.onReload();
				sender.sendMessage(Text.colorize("&7Reloaded config from disk"));
			}
		}
		else if(param.equals("reset")) {
			if(args.length > 1) {
				String targetName = args[1];
				UUID uuid = null;
				OfflinePlayer target = PlayerTools.getPlayer(targetName);
				
				if(target != null)
					uuid = target.getUniqueId();
				else {
					try {
						uuid = UUID.fromString(targetName);
					}
					catch(IllegalArgumentException e) {
						@SuppressWarnings("deprecation")
						OfflinePlayer t = Bukkit.getOfflinePlayer(targetName);
						if(t.hasPlayedBefore())
							uuid = t.getUniqueId();
					}
				}
				
				if(uuid != null) {
					PlayerStatus status = QuestWorldPlugin.getImpl().getPlayerStatus(uuid);
					int c_id = -1;
					int q_id = -1;
					if(args.length <= 2) {
						for(ICategory cat : QuestWorld.getFacade().getCategories())
							status.getTracker().clearCategory(cat);
						
						return true;
					}
					else try {
						c_id = Integer.parseInt(args[2]);
					}
					catch(NumberFormatException exception) {
						sender.sendMessage(Text.colorize("&cError: invalid number for category (", args[2], ")"));
						return true;
					}
					
					if(args.length > 3) try {
						q_id = Integer.parseInt(args[3]);
					}
					catch(NumberFormatException exception) {
						sender.sendMessage(Text.colorize("&cError: invalid number for quest (", args[3], ")"));
						return true;
					}
					
					ICategory category = QuestWorld.getFacade().getCategory(c_id);
					if (category != null)  {
						if (q_id >= 0) {
							IQuest quest = category.getQuest(q_id);
							if(quest != null)
								status.getTracker().clearQuest(quest);
							else
								sender.sendMessage(Text.colorize("&cMissing quest for index ", args[3], " (in category ", args[2], ")"));
						}
						else
							status.getTracker().clearCategory(category);
					}
					else
						sender.sendMessage(Text.colorize("&cMissing category for index ", args[2]));
				}
				else
					sender.sendMessage(Text.colorize("&cCould not find player \""+targetName+"\""));
			}
			else
				sender.sendMessage(Text.colorize("&c/"+label+" reset <player|uuid> [category_id [quest_id]]"));
		}
		else if(param.equals("upgrade")) {
			if(args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
				int changeCount = 0;
				for(ICategory category : QuestWorld.getFacade().getCategories())
					for(IQuest quest : category.getQuests())
						if(quest.getCooldown() == 0) {
							// Administrative process - bypass events and directly modify quest
							// 99% of the time you should use .getState() and .apply()
							IQuestState q = (IQuestState)quest;
							q.setRawCooldown(-1);
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
		else {
			help(label, sender);
		}
		return true;
	}

}
