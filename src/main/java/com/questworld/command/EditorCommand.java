package com.questworld.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.GuideBook;
import com.questworld.QuestingImpl;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IQuest;
import com.questworld.api.menu.PagedMapping;
import com.questworld.api.menu.QuestBook;
import com.questworld.manager.PlayerStatus;
import com.questworld.util.PlayerTools;
import com.questworld.util.Reflect;
import com.questworld.util.Text;
import com.questworld.util.VersionAdapter;

public class EditorCommand implements CommandExecutor {
	private static final int PER_PAGE = 7;
	private final QuestingImpl api;

	public EditorCommand(QuestingImpl api) {
		this.api = api;
	}

	private void help(String label, CommandSender sender) {
		String version = api.getPlugin().getDescription().getVersion();
		sender.sendMessage(Text.colorize("&3== &b/", label, " help &3: &bv" + version + " &3== "));
		sender.sendMessage(Text.colorize("  &bgui &7- Open the editor gui"));
		sender.sendMessage(Text.colorize("  &bbook [player] &7- Give yourself (or player) a questbook"));
		sender.sendMessage(Text.colorize("  &breload &7- Reloads config files from disk"));
		sender.sendMessage(Text.colorize("  &bsave &7- Save all in-game config and quest changes to disk"));
		sender.sendMessage(Text.colorize("  &bextension &7- View extensions"));
		sender.sendMessage(Text.colorize("  &bexport <file> &7- Save all quests to a preset"));
		sender.sendMessage(Text.colorize("  &bimport <file> &4&l*&7 - Overwrite all quests with a preset"));
		sender.sendMessage(Text.colorize("  &bdiscard &4&l*&7 - Reloads quest data from disk, losing changes"));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0)
			help(label, sender);
		
		else {
			switch(args[0].toLowerCase(Locale.US)) {
				case "import":
					importCmd(sender, label, args);
					break;
					
				case "export":
					exportCmd(sender, label, args);
					break;
					
				case "extension":
					ExtensionControl.func(sender, cmd, label, Arrays.copyOfRange(args, 1, args.length));
					break;
					
				case "gui":
					guiCmd(sender);
					break;
					
				case "book":
					bookCmd(sender, args);
					break;
					
				case "discard":
					api.onDiscard();
					api.getFacade().load();
					sender.sendMessage(Text.colorize("&7Reloaded all quests from disk"));
					break;
					
				case "save":
					api.onSave();
					sender.sendMessage(Text.colorize("&7Saved all quests to disk"));
					break;
					
				case "reload":
					api.onReload();
					sender.sendMessage(Text.colorize("&7Reloaded config from disk"));
					break;
					
				case "reset":
					resetCmd(sender, label, args);
					break;
					
				case "progress":
					progressCmd(sender, label, args);
					break;
					
				case "adapter":
					VersionAdapter adapter = Reflect.getAdapter();
	
					sender.sendMessage("Verison(s) " + adapter.toString());
					
					if(sender instanceof Player) {
						ItemStack head = new ItemStack(Material.SKULL_ITEM);
						ItemStack egg = new ItemStack(Material.MONSTER_EGG);
						adapter.makePlayerHead(head, (Player)sender);
						adapter.makeSpawnEgg(egg, EntityType.PIG);
						adapter.sendActionbar((Player)sender, "test");
						adapter.shapelessRecipe("testing", new ItemStack(Material.STONE));
					}
					
					break;
				
				case "help":
				default:
					help(label, sender);
					break;
			}
		}
			
		return true;
	}

	public void importCmd(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(Text.colorize("&cMissing argument: /", label, " import &6<File>"));
			return;
		}
		args[1] += ".zip";
		if (api.presets().load(args[1]))
			sender.sendMessage(Text.colorize("&7Successfully installed the Preset &a", args[1]));
		else
			sender.sendMessage(Text.colorize("&cThe Preset &4", args[1], " &ccould not be installed"));
	}

	public void exportCmd(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(Text.colorize("&cMissing argument: /", label, " export &6<File>"));
			return;
		}
		args[1] += ".zip";
		if (api.presets().save(args[1]))
			sender.sendMessage(Text.colorize("&7Successfully saved the Preset &a", args[1]));
		else
			sender.sendMessage(Text.colorize("&cCould not save Preset &a", args[1]));
	}
	
	public void guiCmd(CommandSender sender) {
		if (sender instanceof Player) {
			PagedMapping.clearPages((Player)sender);
			QuestBook.openCategoryList((Player)sender);
		}
		else
			sender.sendMessage(Text.colorize("&4You are not a Player"));
	}
	
	public void bookCmd(CommandSender sender, String[] args) {
		Player recipient = null;
		String errorMsg = "&cNo player provided";
		
		if(sender instanceof Player)
			recipient = (Player)sender;
		
		if (args.length > 1) {
			recipient = PlayerTools.getPlayer(args[1]);
			errorMsg = "&cCould not find player &e" + args[1];
		}

		if (recipient != null) {
			recipient.getInventory().addItem(GuideBook.instance().item());
			if (sender != recipient)
				sender.sendMessage(Text.colorize("&3Given QuestBook to &b" + recipient.getName()));
			recipient.sendMessage(Text.colorize("&3Recieved QuestBook"));
		}
		else
			sender.sendMessage(Text.colorize(errorMsg));
	}
	
	public void resetCmd(CommandSender sender, String label, String[] args) {
		if (args.length > 1) {
			String targetName = args[1];
			UUID uuid = null;
			OfflinePlayer target = PlayerTools.getPlayer(targetName);

			if (target != null)
				uuid = target.getUniqueId();
			else {
				try {
					uuid = UUID.fromString(targetName);
				}
				catch (IllegalArgumentException e) {
					@SuppressWarnings("deprecation")
					OfflinePlayer t = Bukkit.getOfflinePlayer(targetName);
					if (t.hasPlayedBefore())
						uuid = t.getUniqueId();
				}
			}

			if (uuid != null) {
				PlayerStatus status = (PlayerStatus) QuestWorld.getPlayerStatus(uuid);
				int c_id = -1;
				int q_id = -1;
				if (args.length <= 2) {
					for (ICategory cat : QuestWorld.getFacade().getCategories())
						status.getTracker().clearCategory(cat);

					return;
				}
				else
					try {
						c_id = Integer.parseInt(args[2]);
					}
					catch (NumberFormatException exception) {
						sender.sendMessage(Text.colorize("&cError: invalid number for category (", args[2], ")"));
						return;
					}

				if (args.length > 3)
					try {
						q_id = Integer.parseInt(args[3]);
					}
					catch (NumberFormatException exception) {
						sender.sendMessage(Text.colorize("&cError: invalid number for quest (", args[3], ")"));
						return;
					}

				ICategory category = QuestWorld.getFacade().getCategory(c_id);
				if (category != null) {
					if (q_id >= 0) {
						IQuest quest = category.getQuest(q_id);
						if (quest != null)
							status.getTracker().clearQuest(quest);
						else
							sender.sendMessage(Text.colorize("&cMissing quest for index ", args[3],
									" (in category ", args[2], ")"));
					}
					else
						status.getTracker().clearCategory(category);
				}
				else
					sender.sendMessage(Text.colorize("&cMissing category for index ", args[2]));
			}
			else
				sender.sendMessage(Text.colorize("&cCould not find player \"" + targetName + "\""));
		}
		else
			sender.sendMessage(Text.colorize("&c/" + label + " reset <player|uuid> [category_id [quest_id]]"));
	}
	
	public void progressCmd(CommandSender sender, String label, String[] args) {
		int index = 1;

		UUID uuid = null;
		boolean reset = false;
		ICategory category = null;
		IQuest quest = null;
		int page = 0;
		
		if(sender instanceof Player)
			uuid = ((Player)sender).getUniqueId();

		if (args.length > index) {
			UUID u2 = PlayerTools.findUUID(args[index]).orElse(null);
			if (u2 != null) {
				++index;
				uuid = u2;
			}
		}

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

			if (tail.equals("reset"))
				reset = true;

			else if (tail.equals("page")) {
				page = -1;
				if (args.length > index + 1) {
					try {
						page = Integer.parseInt(args[index + 1]) - 1;
					}
					catch (NumberFormatException e) {
					}
				}

				if (page < 0) {
					// error
					return;
				}
			}
			else {
				// Errors here
				return;
			}
		}

		PlayerStatus status = (PlayerStatus) QuestWorld.getPlayerStatus(uuid);

		if (category != null) {
			if (quest != null) {
				if (reset)
					status.getTracker().clearQuest(quest);

				else {
					List<? extends IMission> missions = quest.getOrderedMissions();
					int end = Math.min(PER_PAGE * (page + 1), missions.size());

					sender.sendMessage(Text
							.colorize("&3Missions - page " + (page + 1) + "/" + (missions.size() / PER_PAGE + 1)));
					for (int i = PER_PAGE * page; i < end; ++i) {
						IMission m = missions.get(i);
						sender.sendMessage(Text.colorize(m.getText(),
								" &7- &a" + status.getProgress(m) + "/" + m.getAmount()));
					}
				}
			}
			else if (reset)
				status.getTracker().clearCategory(category);

			else {
				ArrayList<? extends IQuest> quests = new ArrayList<>(category.getQuests());
				Collections.sort(quests, (l, r) -> l.getID() - r.getID());
				int end = Math.min(PER_PAGE * (page + 1), quests.size());

				sender.sendMessage(
						Text.colorize("&3Quests - page " + (page + 1) + "/" + (quests.size() / PER_PAGE + 1)));
				for (int i = PER_PAGE * page; i < end; ++i) {
					IQuest q = quests.get(i);
					sender.sendMessage(Text.colorize(q.getID() + ": " + q.getName() + " &7- &a"
							+ status.getProgress(q) + "/" + q.getMissions().size()));
				}
			}
		}
		else if (reset)
			for (ICategory cat : QuestWorld.getFacade().getCategories())
				status.getTracker().clearCategory(cat);
		else {
			ArrayList<? extends ICategory> categories = new ArrayList<>(QuestWorld.getFacade().getCategories());
			Collections.sort(categories, (l, r) -> l.getID() - r.getID());
			int end = Math.min(PER_PAGE * (page + 1), categories.size());

			sender.sendMessage(
					Text.colorize("&3Categories - page " + (page + 1) + "/" + (categories.size() / PER_PAGE + 1)));

			for (int i = PER_PAGE * page; i < end; ++i) {
				ICategory c = categories.get(i);
				sender.sendMessage(Text.colorize(c.getID() + ": " + c.getName() + " &7- &a" + status.getProgress(c)
						+ "/" + c.getQuests().size()));
			}
		}
	}
}
