package me.mrCookieSlime.QuestWorld.quests;

import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuOpeningHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.menu.Buttons;
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

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QBDialogue {
	

	public static void openDeletionConfirmation(Player p, final QuestingObject q) {
		ChestMenu menu = new ChestMenu(Text.colorize("&4&lAre you Sure?"));
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().DestructiveWarning().playTo(p);
			}
		});
		
		ItemBuilder wool = new ItemBuilder(Material.WOOL);
		
		menu.addItem(6, wool.color(DyeColor.RED).display("&cNo").getNew());
		menu.addMenuClickHandler(6, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				if (q instanceof Quest) QuestBook.openCategoryEditor(p, ((Quest) q).getCategory());
				else if (q instanceof Category) QuestBook.openEditor(p);
				else if (q instanceof Mission) QuestBook.openQuestEditor(p, ((Mission) q).getQuest());
				return false;
			}
		});
		
		String tag = Text.colorize("&r") ;
		if (q instanceof Quest) tag += "your Quest \"" + ((Quest) q).getName() + "\"";
		else if (q instanceof Category) tag += "your Category \"" + ((Category) q).getName() + "\"";
		else if (q instanceof Mission) tag += "your Task";
		
		menu.addItem(2, wool.color(DyeColor.LIME).display("&aYes I am sure").lore("", "&rThis will delete", tag).getNew());
		menu.addMenuClickHandler(2, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				QuestWorld.getSounds().DestructiveClick().playTo(p);
				QuestWorld.getSounds().muteNext();
				if (q instanceof Category) {
					Category category = (Category)q;
					if(CancellableEvent.send(new CategoryDeleteEvent(category))) {
						QuestWorld.getInstance().unregisterCategory(category);
						p.closeInventory();
						QuestBook.openEditor(p);
						PlayerTools.sendTranslation(p, true, Translation.category_deleted, q.getName());
					}
				}
				else if (q instanceof Quest) {
					Quest quest = (Quest)q;
					if(CancellableEvent.send(new QuestDeleteEvent(quest))) {
						PlayerManager.clearAllQuestData(quest);
						quest.getCategory().removeQuest(quest);
						p.closeInventory();
						QuestBook.openCategoryQuestEditor(p, quest.getCategory());
						PlayerTools.sendTranslation(p, true, Translation.quest_deleted, q.getName());
					}
				}
				else if (q instanceof Mission) {
					Mission mission = (Mission)q;
					if(CancellableEvent.send(new MissionDeleteEvent(mission))) {
						mission.getQuest().removeMission(mission);
						p.closeInventory();
						QuestBook.openQuestEditor(p, mission.getQuest());
					}
				}
				return false;
			}
		});
		
		menu.open(p);
	}

	public static void openResetConfirmation(Player p, final Quest q) {
		ChestMenu menu = new ChestMenu(Text.colorize("&4&lAre you Sure?"));
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().DestructiveWarning().playTo(p);
			}
		});
		
		ItemBuilder wool = new ItemBuilder(Material.WOOL);
		
		menu.addItem(6, wool.color(DyeColor.RED).display("&cNo").getNew());
		menu.addMenuClickHandler(6, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				QuestBook.openQuestEditor(p, q);
				return false;
			}
		});
		
		menu.addItem(2, wool.color(DyeColor.LIME).display("&aYes I am sure").lore("", "&rThis will reset this Quest's Database").getNew());
		menu.addMenuClickHandler(2, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				PlayerManager.clearAllQuestData(q);
				QuestBook.openQuestEditor(p, q);
				return false;
			}
		});
		
		menu.open(p);
	}
	
	public static void openQuestMissionEntityEditor(Player p, final Mission mission) {
		openQuestMissionEntityEditor(p, mission, 0, 0);
	}
	
	private static void openQuestMissionEntityEditor(Player p, final Mission mission, int page, int mode) {
		List<EntityType> entities = EntityTools.listAliveEntityTypes();
		/*
		final String[] sortingMethods = {
				"By Type",
				"A to Z",
				"Z to A"
		};
		switch(mode) {
		case 0:
			// Already sorted this way in initialization
			break;
		case 1:
			entities.sort(EntityTools.NameComp.forward());
			break;
		case 2:
			entities.sort(EntityTools.NameComp.backward());
			break;
		}
		
		int lastPage = entities.size() / 45; // Double chest size without last row
		*/
		//String title = Text.colorize(mission.getQuest().getName() + " &7- &8(Page " + (page+1) + "/" + (lastPage+1) + ")");
		String title = mission.getQuest().getName();
		final ChestMenu menu = new ChestMenu(title);
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		String[] lore = {"", "&e> Click to select"};
		
		PagedMapping pager = new PagedMapping(45);
		
		for(int i = 0; i < entities.size(); ++i) {
			EntityType entity = entities.get(i);
			ItemBuilder builder = new ItemBuilder(EntityTools.getEntityDisplay(entity))
					.lore(lore)
					.display("&7Entity Type: &r" + Text.niceName(entity.name()));
			pager.addItem(i, builder.get());
			pager.addNavButton(i, new MenuClickHandler() {
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					mission.setEntity(entity);
					QuestBook.openQuestMissionEditor(p, mission);
					return false;
				}
			});
		}
		pager.setBackButton(Buttons.simpleHandler(event -> QuestBook.openQuestMissionEditor(p, mission)));
		pager.build(menu, p);
		menu.open(p);
	}

	public static void openCommandEditor(Player p, Quest quest) {
		try {
			p.sendMessage(Text.colorize("&7&m----------------------------"));
			for (int i = 0; i < quest.getCommands().size(); i++) {
				String command = quest.getCommands().get(i);
				new TellRawMessage(Text.colorize("&4X &7") + command).addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to remove this Command")).addClickEvent(me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.ClickAction.RUN_COMMAND, "/questeditor delete_command " + quest.getCategory().getID() + " " + quest.getID() + " " + i).send(p);
			}
			new TellRawMessage(Text.colorize("&2+ &7Add more Commands... (Click)")).addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to add a new Command")).addClickEvent(me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.ClickAction.RUN_COMMAND, "/questeditor add_command " + quest.getCategory().getID() + " " + quest.getID()).send(p);
			p.sendMessage(Text.colorize("&7&m----------------------------"));
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void openQuestRequirementChooser(Player p, final QuestingObject quest) {
		ChestMenu menu = new ChestMenu("&c&lQuest Editor");
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		

		PagedMapping pager = new PagedMapping(45, 9);
		for(Category category : QuestWorld.getInstance().getCategories()) {
			int i = category.getID();
			ItemStack item = new ItemBuilder(category.getItem()).lore(
					"",
					"&7&oLeft Click to open").get();
			pager.addItem(i, item);
			pager.addNavButton(i, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {

					QuestWorld.getInstance().getManager(p).putPage(0);
					openQuestRequirementChooser2(p, quest, category);
					return false;
				}
			});
		}
		pager.setBackButton(Buttons.simpleHandler(event -> {
			if(quest instanceof Quest)
				QuestBook.openQuestEditor(p, (Quest)quest);
			else
				QuestBook.openCategoryEditor(p, (Category)quest);
		}));
		pager.build(menu, p);
		menu.open(p);
	}

	public static void openQuestRequirementChooser2(Player p, final QuestingObject q, Category category) {
		ChestMenu menu = new ChestMenu("&c&lQuest Editor");
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		PagedMapping pager = new PagedMapping(45, 9);
		for(Quest quest : category.getQuests()) {
			int i = quest.getID();
			ItemStack item = new ItemBuilder(quest.getItem()).lore(
					"",
					"&7&oClick to select it as a Requirement",
					"&7&ofor the Quest:",
					"&r" + q.getName()).get();
			pager.addItem(i, item);
			pager.addButton(i, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					QuestWorld.getInstance().getManager(p).popPage();
					q.setParent(quest);
					if (q instanceof Quest) QuestBook.openQuestEditor(p, (Quest) q);
					else QuestBook.openCategoryEditor(p, (Category) q);
					return false;
				}
			});
		}
		pager.setBackButton(Buttons.simpleHandler(event -> openQuestRequirementChooser(p, q)));
		pager.build(menu, p);
		menu.open(p);
	}

}
