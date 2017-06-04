package me.mrCookieSlime.QuestWorld.listeners;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.CategoryChange;
import me.mrCookieSlime.QuestWorld.api.QuestChange;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.parties.Party;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
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
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		Input input = QuestWorld.getInstance().getInput(e.getPlayer().getUniqueId());
		switch(input.getType()) {
		
		case CATEGORY_CREATION: {
			new Category(e.getMessage(), (Integer) input.getValue());
			PlayerTools.sendTranslation(e.getPlayer(), true, Translation.category_created, e.getMessage());
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openEditor(e.getPlayer());
			break;
		}
		
		case CATEGORY_RENAME: {
			Category category = (Category) input.getValue();
			CategoryChange changes = new CategoryChange(category);
			changes.setName(e.getMessage());
			if(changes.sendEvent()) {
				String oldName = category.getName();
				changes.apply();
				PlayerTools.sendTranslation(e.getPlayer(), true, Translation.category_nameset, e.getMessage(), oldName);
			}
			
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openCategoryEditor(e.getPlayer(), category);
			break;
		}
		
		case QUEST_RENAME: {
			Quest quest = (Quest) input.getValue();
			QuestChange changes = new QuestChange(quest);
			changes.setName(e.getMessage());
			if(changes.sendEvent()) {
				String oldName = quest.getName();
				changes.apply();
				PlayerTools.sendTranslation(e.getPlayer(), true, Translation.quest_nameset, e.getMessage(), oldName);
			}
			
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestEditor(e.getPlayer(), quest);
			break;
		}
		
		case QUEST_CREATION: {
			new Quest(e.getMessage(), (String) input.getValue());
			PlayerTools.sendTranslation(e.getPlayer(), true, Translation.quest_created, e.getMessage());
			e.setCancelled(true);
			QuestBook.openCategoryQuestEditor(e.getPlayer(), QuestWorld.getInstance().getCategory(Integer.parseInt(((String) input.getValue()).split(" M ")[0])));
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			break;
		}
		
		case PARTY_INVITE: {
			Party party = (Party) input.getValue();
			String name = Text.decolor(e.getMessage()).replace("@", "");
			@SuppressWarnings("deprecation")
			Player player = Bukkit.getPlayer(name);
			if (player != null) {
				if (QuestWorld.getInstance().getManager(player).getParty() == null) {
					PlayerTools.sendTranslation(e.getPlayer(), true, Translation.party_playeradd, name);
					try {
						party.invitePlayer(player);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				else PlayerTools.sendTranslation(e.getPlayer(), true, Translation.party_errormember, name);
			}
			else {
				PlayerTools.sendTranslation(e.getPlayer(), true, Translation.party_errorabsent, name);
			}
			e.setCancelled(true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			break;
		}
		
		case KILL_NAMED: {
			Mission mission = (Mission) input.getValue();
			mission.setEntityName(Text.colorize(e.getMessage()));
			PlayerTools.sendTranslation(e.getPlayer(), true, Translation.killtype_rename);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestMissionEditor(e.getPlayer(), mission);
			break;
		}
		
		case CITIZEN: {
			Mission mission = (Mission) input.getValue();
			mission.setEntityName(Text.colorize(e.getMessage()));
			PlayerTools.sendTranslation(e.getPlayer(), true, Translation.citizen_rename);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestMissionEditor(e.getPlayer(), mission);
			break;
		}
		
		case LOCATION_NAME: {
			Mission mission = (Mission) input.getValue();
			mission.setEntityName(Text.colorize(e.getMessage()));
			PlayerTools.sendTranslation(e.getPlayer(), true, Translation.location_rename);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openQuestMissionEditor(e.getPlayer(), mission);
			break;
		}
		
		case COMMAND_ADD: {
			Quest quest = (Quest) input.getValue();
			QuestChange changes = new QuestChange(quest);
			changes.addCommand(ChatColor.stripColor(e.getMessage()));
			if(changes.sendEvent())
				changes.apply();

			QBDialogue.openCommandEditor(e.getPlayer(), quest);
			e.setCancelled(true);
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			break;
		}
		
		case CATEGORY_PERMISSION: {
			Category category = (Category) input.getValue();
			CategoryChange changes = new CategoryChange(category);
			String permission = e.getMessage().equalsIgnoreCase("none") ? "": e.getMessage();
			changes.setPermission(permission);
			if(changes.sendEvent()) {
				String oldPerm = category.getPermission();
				changes.apply();
				PlayerTools.sendTranslation(e.getPlayer(), true, Translation.category_permset, category.getName(), e.getMessage(), oldPerm);
			}
			
			QuestWorld.getInstance().removeInput(e.getPlayer().getUniqueId());
			e.setCancelled(true);
			QuestBook.openCategoryEditor(e.getPlayer(), category);
			break;
		}
		
		case QUEST_PERMISSION: {
			Quest quest = (Quest) input.getValue();
			QuestChange changes = new QuestChange(quest);
			String permission = e.getMessage().equalsIgnoreCase("none") ? "": e.getMessage();
			changes.setPermission(permission);
			if(changes.sendEvent()) {
				String oldPerm = quest.getPermission();
				changes.apply();
				PlayerTools.sendTranslation(e.getPlayer(), true, Translation.quest_permset, quest.getName(), e.getMessage(), oldPerm);
			}
			
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
