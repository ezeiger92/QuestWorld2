package me.mrCookieSlime.QuestWorld.quests;

import java.util.List;

//import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
//import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.containers.PagedMapping;
import me.mrCookieSlime.QuestWorld.events.CancellableEvent;
import me.mrCookieSlime.QuestWorld.events.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.events.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.events.QuestDeleteEvent;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import me.mrCookieSlime.QuestWorld.utils.EntityTools;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class QBDialogue {
	public static void openDeletionConfirmation(Player p, final QuestingObject q) {
		QuestWorld.getSounds().DestructiveWarning().playTo(p);
		
		Menu menu = new Menu(1, "&4&lAre you Sure?");
		
		menu.put(6, ItemBuilder.Proto.RED_WOOL.get().display("&cNo").get(), event -> {
			Player p2 = (Player) event.getWhoClicked();
			if (q instanceof Quest) QuestBook.openCategoryEditor(p2, ((Quest) q).getCategory());
			else if (q instanceof Category) QuestBook.openEditor(p2);
			else if (q instanceof Mission) QuestBook.openQuestEditor(p2, ((Mission) q).getQuest());
		});
		
		String tag = Text.colorize("&r") ;
		if (q instanceof Quest) tag += "your Quest \"" + ((Quest) q).getName() + "\"";
		else if (q instanceof Category) tag += "your Category \"" + ((Category) q).getName() + "\"";
		else if (q instanceof Mission) tag += "your Task";
		
		menu.put(2,
				ItemBuilder.Proto.LIME_WOOL.get()
				.display("&aYes I am sure")
				.lore("", "&rThis will delete", tag).get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					QuestWorld.getSounds().DestructiveClick().playTo(p2);
					QuestWorld.getSounds().muteNext();
					if (q instanceof Category) {
						Category category = (Category)q;
						if(CancellableEvent.send(new CategoryDeleteEvent(category))) {
							QuestWorld.getInstance().unregisterCategory(category);
							p2.closeInventory();
							QuestBook.openEditor(p2);
							PlayerTools.sendTranslation(p2, true, Translation.category_deleted, q.getName());
						}
					}
					else if (q instanceof Quest) {
						Quest quest = (Quest)q;
						if(CancellableEvent.send(new QuestDeleteEvent(quest))) {
							PlayerManager.clearAllQuestData(quest);
							quest.getCategory().removeQuest(quest);
							p2.closeInventory();
							QuestBook.openCategoryQuestEditor(p2, quest.getCategory());
							PlayerTools.sendTranslation(p2, true, Translation.quest_deleted, q.getName());
						}
					}
					else if (q instanceof Mission) {
						Mission mission = (Mission)q;
						if(CancellableEvent.send(new MissionDeleteEvent(mission))) {
							mission.getQuest().removeMission(mission);
							p2.closeInventory();
							QuestBook.openQuestEditor(p2, mission.getQuest());
						}
					}
				}
		);
		
		menu.openFor(p);
	}

	public static void openResetConfirmation(Player p, final Quest q) {
		QuestWorld.getSounds().DestructiveWarning().playTo(p);
		
		Menu menu = new Menu(1, "&4&lAre you Sure?");
		
		menu.put(6, ItemBuilder.Proto.RED_WOOL.get().display("&cNo").get(), event -> {
			QuestBook.openQuestEditor((Player) event.getWhoClicked(), q);
		});
		
		menu.put(2, ItemBuilder.Proto.LIME_WOOL.get()
				.display("&aYes I am sure")
				.lore("", "&rThis will reset this Quest's Database").get(),
				event -> {
					PlayerManager.clearAllQuestData(q);
					QuestBook.openQuestEditor((Player) event.getWhoClicked(), q);
				}
		);
		
		menu.openFor(p);
	}
	
	public static void openQuestMissionEntityEditor(Player p, final Mission mission) {
		QuestWorld.getSounds().EditorClick().playTo(p);
		
		//String title = Text.colorize(mission.getQuest().getName() + " &7- &8(Page " + (page+1) + "/" + (lastPage+1) + ")");
		final Menu menu = new Menu(6, "&3Entity Selector: " + mission.getQuest().getName());
		
		String[] lore = {"", "&e> Click to select"};
		
		PagedMapping pager = new PagedMapping(45);

		List<EntityType> entities = EntityTools.listAliveEntityTypes();
		for(int i = 0; i < entities.size(); ++i) {
			EntityType entity = entities.get(i);
			pager.addNavButton(i,
					new ItemBuilder(EntityTools.getEntityDisplay(entity))
					.lore(lore)
					.display("&7Entity Type: &r" + Text.niceName(entity.name())).get(),
					event -> {
						mission.setEntity(entity);
						QuestBook.openQuestMissionEditor((Player) event.getWhoClicked(), mission);
					}
			);
		}
		pager.setBackButton(event -> QuestBook.openQuestMissionEditor(p, mission));
		pager.build(menu, p);
		menu.openFor(p);
	}

	public static void openCommandEditor(Player p, Quest quest) {
		try {
			p.sendMessage(Text.colorize("&7&m----------------------------"));
			for (int i = 0; i < quest.getCommands().size(); i++) {
				String command = quest.getCommands().get(i).replaceAll("('|\"|\\\\)", "\\\\$1");
				
				//new TellRawMessage(Text.colorize("&4X &7") + command).addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to remove this Command")).addClickEvent(me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.ClickAction.RUN_COMMAND, "/questeditor delete_command " + quest.getCategory().getID() + " " + quest.getID() + " " + i).send(p);
				PlayerTools.tellraw(p, Text.colorize("['', {"
						+ "'text':'&4X &7" + command + "',"
						+ "'clickEvent':{'action':'run_command','value':'/questeditor delete_command " + quest.getCategory().getID() + " " + quest.getID() + " " + i + "'},"
						+ "'hoverEvent':{'action':'show_text','value':'&7Click to remove this Command'}}]")
						.replaceAll("(?<!\\\\)'", "\\\"").replaceAll("\\\\'", "'"));
			}
			//new TellRawMessage(Text.colorize("&2+ &7Add more Commands... (Click)")).addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to add a new Command")).addClickEvent(me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.ClickAction.RUN_COMMAND, "/questeditor add_command " + quest.getCategory().getID() + " " + quest.getID()).send(p);
			PlayerTools.tellraw(p, Text.colorize("['', {"
					+ "'text':'&2+ &7Add more Commands... (Click)',"
					+ "'clickEvent':{'action':'run_command','value':'/questeditor add_command " + quest.getCategory().getID() + " " + quest.getID() + "'},"
					+ "'hoverEvent':{'action':'show_text','value':'&7Click to add a new Command'}}]")
					.replace('\'', '"'));
			p.sendMessage(Text.colorize("&7&m----------------------------"));
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void openQuestRequirementChooser(Player p, final QuestingObject quest) {
		QuestWorld.getSounds().EditorClick().playTo(p);
		
		Menu menu = new Menu(1, "&c&lQuest Editor");

		PagedMapping pager = new PagedMapping(45, 9);
		for(Category category : QuestWorld.getInstance().getCategories()) {
			pager.addNavButton(category.getID(), new ItemBuilder(category.getItem()).lore(
					"",
					"&7&oLeft Click to open").get(),
					event -> {
						Player p2 = (Player)event.getWhoClicked();
						QuestWorld.getInstance().getManager(p2).putPage(0);
						openQuestRequirementChooser2(p2, quest, category);
					}
			);
		}
		pager.setBackButton(event -> {
			if(quest instanceof Quest)
				QuestBook.openQuestEditor(p, (Quest)quest);
			else
				QuestBook.openCategoryEditor(p, (Category)quest);
		});
		pager.build(menu, p);
		menu.openFor(p);
	}

	public static void openQuestRequirementChooser2(Player p, final QuestingObject q, Category category) {
		QuestWorld.getSounds().EditorClick().playTo(p);
		
		Menu menu = new Menu(1, "&c&lQuest Editor");
		
		PagedMapping pager = new PagedMapping(45, 9);
		for(Quest quest : category.getQuests()) {
			pager.addButton(quest.getID(),
					new ItemBuilder(quest.getItem()).lore(
							"",
							"&7&oClick to select it as a Requirement",
							"&7&ofor the Quest:",
							"&r" + q.getName()).get(),
					event -> {
						Player p2 = (Player) event.getWhoClicked();
						QuestWorld.getInstance().getManager(p2).popPage();
						q.setParent(quest);
						if (q instanceof Quest) QuestBook.openQuestEditor(p2, (Quest) q);
						else QuestBook.openCategoryEditor(p2, (Category) q);
					}
			);
		}
		pager.setBackButton(event -> openQuestRequirementChooser(p, q));
		pager.build(menu, p);
		menu.openFor(p);
	}
}
