package me.mrCookieSlime.QuestWorld.listeners;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Party;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EditorListener implements Listener {

	public EditorListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		Input input = QuestWorld.getInstance().getInput(e.getPlayer().getUniqueId());
		switch(input.getType()) {
		
		case CATEGORY_CREATION: {
			new Category(e.getMessage(), (Integer) input.getValue());
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.new-category", true, new Variable("%name%", e.getMessage()));
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openEditor(e.getPlayer());
			break;
		}
		
		case CATEGORY_RENAME: {
			Category category = (Category) input.getValue();
			category.setName(e.getMessage());
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.renamed-category", true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openCategoryEditor(e.getPlayer(), category);
			break;
		}
		
		case QUEST_RENAME: {
			Quest quest = (Quest) input.getValue();
			quest.setName(e.getMessage());
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.renamed-quest", true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestEditor(e.getPlayer(), quest);
			break;
		}
		
		case QUEST_CREATION: {
			new Quest(e.getMessage(), (String) input.getValue());
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.new-category", true, new Variable("%name%", e.getMessage()));
			e.setCancelled(true);
			QuestBook.openCategoryQuestEditor(e.getPlayer(), QuestWorld.getInstance().getCategory(Integer.parseInt(((String) input.getValue()).split(" M ")[0])));
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			break;
		}
		
		case PARTY_INVITE: {
			Party party = (Party) input.getValue();
			String name = Text.decolor(e.getMessage()).replace("@", "");
			Player player = Bukkit.getPlayer(name);
			if (player != null) {
				if (QuestWorld.getInstance().getManager(player).getParty() == null) {
					QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "party.invited", true, new Variable("%name%", name));
					try {
						party.invite(player);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				else QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "party.already", true, new Variable("%name%", name));
			}
			else {
				QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "party.not-online", true, new Variable("%name%", name));
			}
			e.setCancelled(true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			break;
		}
		
		case KILL_NAMED: {
			QuestMission mission = (QuestMission) input.getValue();
			mission.setEntityName(Text.colorize(e.getMessage()));
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.renamed-kill-type", true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestMissionEditor(e.getPlayer(), mission);
			break;
		}
		
		case CITIZEN: {
			QuestMission mission = (QuestMission) input.getValue();
			mission.setEntityName(Text.colorize(e.getMessage()));
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.renamed-citizen", true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestMissionEditor(e.getPlayer(), mission);
			break;
		}
		
		case LOCATION_NAME: {
			QuestMission mission = (QuestMission) input.getValue();
			mission.setEntityName(Text.colorize(e.getMessage()));
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.renamed-location", true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestMissionEditor(e.getPlayer(), mission);
			break;
		}
		
		case COMMAND_ADD: {
			Quest quest = (Quest) input.getValue();
			quest.addCommand(ChatColor.stripColor(e.getMessage()));
			QBDialogue.openCommandEditor(e.getPlayer(), quest);
			e.setCancelled(true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			break;
		}
		
		case CATEGORY_PERMISSION: {
			Category category = (Category) input.getValue();
			String permission = e.getMessage().equalsIgnoreCase("none") ? "": e.getMessage();
			category.setPermission(permission);
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.permission-set-category", true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openCategoryEditor(e.getPlayer(), category);
			break;
		}
		
		case QUEST_PERMISSION: {
			Quest quest = (Quest) input.getValue();
			String permission = e.getMessage().equalsIgnoreCase("none") ? "": e.getMessage();
			quest.setPermission(permission);
			QuestWorld.getInstance().getLocalization().sendTranslation(e.getPlayer(), "editor.permission-set-quest", true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestEditor(e.getPlayer(), quest);
			break;
		}
		
		case NONE:
			break;
		default:
			break;
		}
	}
	
}
