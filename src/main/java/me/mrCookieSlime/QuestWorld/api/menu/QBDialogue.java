package me.mrCookieSlime.QuestWorld.api.menu;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryState;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.api.contract.IStateful;
import me.mrCookieSlime.QuestWorld.container.PagedMapping;
import me.mrCookieSlime.QuestWorld.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.event.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.event.QuestDeleteEvent;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class QBDialogue {
	public static void openDeletionConfirmation(Player p, final IStateful q) {
		QuestWorld.getSounds().DESTRUCTIVE_WARN.playTo(p);
		
		Menu menu = new Menu(1, "&4&lAre you Sure?");
		
		menu.put(6, ItemBuilder.Proto.RED_WOOL.get().display("&cNo").get(), event -> {
			Player p2 = (Player) event.getWhoClicked();
			if (q instanceof IQuest) QuestBook.openCategoryEditor(p2, ((IQuest) q).getCategory());
			else if (q instanceof ICategory) QuestBook.openEditor(p2);
			else if (q instanceof IMission) QuestBook.openQuestEditor(p2, ((IMission) q).getQuest());
		});
		
		String tag = Text.colorize("&r") ;
		if (q instanceof IQuest) tag += "your Quest \"" + ((IQuest) q).getName() + "\"";
		else if (q instanceof ICategory) tag += "your Category \"" + ((ICategory) q).getName() + "\"";
		else if (q instanceof IMission) tag += "your Task";
		
		menu.put(2,
				ItemBuilder.Proto.LIME_WOOL.get()
				.display("&aYes I am sure")
				.lore("", "&rThis will delete", tag).get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					QuestWorld.getSounds().DESTRUCTIVE_CLICK.playTo(p2);
					// TODO QuestWorld.getSounds().muteNext();
					if (q instanceof ICategory) {
						ICategory category = (ICategory)q;
						if(CancellableEvent.send(new CategoryDeleteEvent(category))) {
							QuestWorld.get().unregisterCategory(category);
							p2.closeInventory();
							QuestBook.openEditor(p2);
							PlayerTools.sendTranslation(p2, true, Translation.CATEGORY_DELETED, category.getName());
						}
					}
					else if (q instanceof IQuest) {
						IQuest quest = (IQuest)q;
						if(CancellableEvent.send(new QuestDeleteEvent(quest))) {
							PlayerManager.clearAllQuestData(quest);
							
							ICategoryState changes = quest.getCategory().getState();
							changes.removeQuest(quest);
							//changes.apply(); 

							p2.closeInventory();
							QuestBook.openCategoryQuestEditor(p2, quest.getCategory());
							PlayerTools.sendTranslation(p2, true, Translation.QUEST_DELETED, quest.getName());
						}
					}
					else if (q instanceof IMission) {
						IMission mission = (IMission)q;
						if(CancellableEvent.send(new MissionDeleteEvent(mission))) {
							IQuestState changes = mission.getQuest().getState();
							changes.removeMission(mission);
							changes.apply();
							
							p2.closeInventory();
							QuestBook.openQuestEditor(p2, mission.getQuest());
						}
					}
				}
		);
		
		menu.openFor(p);
	}

	public static void openResetConfirmation(Player p, final IQuest q) {
		QuestWorld.getSounds().DESTRUCTIVE_WARN.playTo(p);
		
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
	
	public static void openQuestMissionEntityEditor(Player p, final IMission mission) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		IMissionState changes = mission.getState();
		//String title = Text.colorize(mission.getQuest().getName() + " &7- &8(Page " + (page+1) + "/" + (lastPage+1) + ")");
		final Menu menu = new Menu(6, "&3Entity Selector: " + mission.getQuest().getName());
		
		String[] lore = {"", "&e> Click to select"};
		
		PagedMapping pager = new PagedMapping(45);

		EntityType[] entities = EntityTools.aliveEntityTypes();
		for(int i = 0; i < entities.length; ++i) {
			EntityType entity = entities[i];
			pager.addButton(i,
					EntityTools.getEntityDisplay(entity)
					.lore(lore)
					.display("&7Entity Type: &r" + Text.niceName(entity.name())).get(),
					event -> {
						changes.setEntity(entity);
						changes.apply();
						QuestBook.openQuestMissionEditor((Player) event.getWhoClicked(), mission);
					}, true
			);
		}
		pager.setBackButton(event -> QuestBook.openQuestMissionEditor(p, mission));
		pager.build(menu, p);
		menu.openFor(p);
	}

	public static void openCommandEditor(Player p, IQuest quest) {
		try {
			p.sendMessage(Text.colorize("&7&m----------------------------"));
			for (int i = 0; i < quest.getCommands().size(); i++) {
				String command = quest.getCommands().get(i).replaceAll("(\"|\\\\)", "\\\\$1");
				
				JsonObject redX = new JsonObject();
				redX.addProperty("text", "X ");
				redX.addProperty("color", "dark_red");
				
				JsonObject commandDisplay = new JsonObject();
				commandDisplay.addProperty("text", command);
				commandDisplay.addProperty("color", "gray");
				{
					JsonObject clickEvent = new JsonObject();
					clickEvent.addProperty("action", "run_command");
					clickEvent.addProperty("value",  "/questeditor delete_command " + quest.getCategory().getID() + " " + quest.getID() + " " + i);

					redX.add("clickEvent", clickEvent);
					commandDisplay.add("clickEvent", clickEvent);
				}
				{
					JsonObject hoverEvent = new JsonObject();
					hoverEvent.addProperty("action", "show_text");
					{
						JsonObject hoverText = new JsonObject();
						hoverText.addProperty("text", "Click to remove this Command");
						hoverText.addProperty("color", "gray");

						hoverEvent.add("value",  hoverText);
					}

					redX.add("hoverEvent", hoverEvent);
					commandDisplay.add("hoverEvent", hoverEvent);
				}
				
				PlayerTools.tellraw(p, redX.toString(), commandDisplay.toString());
			}
			
			JsonObject greenPlus = new JsonObject();
			greenPlus.addProperty("text", "+ ");
			greenPlus.addProperty("color", "dark_green");
			
			JsonObject prompt = new JsonObject();
			prompt.addProperty("text", "Add more Commands... (Click)");
			prompt.addProperty("color", "gray");
			{
				JsonObject clickEvent = new JsonObject();
				clickEvent.addProperty("action", "run_command");
				clickEvent.addProperty("value", "/questeditor add_command " + quest.getCategory().getID() + " " + quest.getID());
				
				greenPlus.add("clickEvent", clickEvent);
				prompt.add("clickEvent", clickEvent);
			}
			{
				JsonObject hoverEvent = new JsonObject();
				hoverEvent.addProperty("action", "show_text");
				{
					JsonObject hoverText = new JsonObject();
					hoverText.addProperty("text", "Click to add a new Command");
					hoverText.addProperty("color", "gray");
					
					hoverEvent.add("value", hoverText);
				}
				
				greenPlus.add("hoverEvent", hoverEvent);
				prompt.add("hoverEvent", hoverEvent);
			}
			
			PlayerTools.tellraw(p, greenPlus.toString(), prompt.toString());
			p.sendMessage(Text.colorize("&7&m----------------------------"));
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void openQuestRequirementChooser(Player p, final IStateful quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		Menu menu = new Menu(1, "&c&lQuest Editor");

		PagedMapping pager = new PagedMapping(45, 9);
		for(ICategory category : QuestWorld.get().getCategories()) {
			pager.addButton(category.getID(), new ItemBuilder(category.getItem()).lore(
					"",
					"&7&oLeft Click to open").get(),
					event -> {
						Player p2 = (Player)event.getWhoClicked();
						PlayerManager.of(p2).putPage(0);
						openQuestRequirementChooser2(p2, quest, category);
					}, true
			);
		}
		pager.setBackButton(event -> {
			if(quest instanceof IQuest)
				QuestBook.openQuestEditor(p, (IQuest)quest);
			else
				QuestBook.openCategoryEditor(p, (ICategory)quest);
		});
		pager.build(menu, p);
		menu.openFor(p);
	}

	private static void openQuestRequirementChooser2(Player p, final IStateful q, ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		Menu menu = new Menu(1, "&c&lQuest Editor");
		
		PagedMapping pager = new PagedMapping(45, 9);
		for(IQuest quest : category.getQuests()) {
			pager.addButton(quest.getID(),
					new ItemBuilder(quest.getItem()).lore(
							"",
							"&7&oClick to select it as a Requirement",
							"&7&ofor the Quest:",
							"&r" + quest.getName()).get(),
					event -> {
						Player p2 = (Player) event.getWhoClicked();
						PlayerManager.of(p2).popPage();

						if (q instanceof IQuest) {
							IQuest child = (IQuest)q;
							
							IQuestState changes = child.getState();
							changes.setParent(quest);
							changes.apply();
							
							QuestBook.openQuestEditor(p2, child);
						}
						else {
							ICategory child = (ICategory)q;
							
							ICategoryState changes = child.getState();
							changes.setParent(quest);
							changes.apply();
							
							QuestBook.openCategoryEditor(p2, child);
						}
					}, false
			);
		}
		pager.setBackButton(event -> openQuestRequirementChooser(p, q));
		pager.build(menu, p);
		menu.openFor(p);
	}
}
