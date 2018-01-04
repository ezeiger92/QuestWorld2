package me.mrCookieSlime.QuestWorld.api.menu;

import static me.mrCookieSlime.QuestWorld.util.json.Prop.*;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryState;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;
import me.mrCookieSlime.QuestWorld.api.contract.IStateful;
import me.mrCookieSlime.QuestWorld.api.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.api.event.CategoryDeleteEvent;
import me.mrCookieSlime.QuestWorld.api.event.MissionDeleteEvent;
import me.mrCookieSlime.QuestWorld.api.event.QuestDeleteEvent;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;
import me.mrCookieSlime.QuestWorld.util.json.JsonBlob;
import me.mrCookieSlime.QuestWorld.util.json.Prop;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class QBDialogue {
	public static void openDeletionConfirmation(Player p, final IStateful q) {
		QuestWorld.getSounds().DESTRUCTIVE_WARN.playTo(p);
		
		Menu menu = new Menu(1, "&4&lAre you Sure?");
		
		menu.put(6, ItemBuilder.Proto.RED_WOOL.get().display("&cNo").get(), event -> {
			Player p2 = (Player) event.getWhoClicked();
			if (q instanceof IQuest) QuestBook.openCategoryEditor(p2, ((IQuest) q).getCategory());
			else if (q instanceof ICategory) QuestBook.openCategoryList(p2);
			else if (q instanceof IMission) QuestBook.openQuestEditor(p2, ((IMission) q).getQuest());
		});
		
		String tag;
		if (q instanceof IQuest) tag = "your Quest \"" + ((IQuest) q).getName() + "\"";
		else if (q instanceof ICategory) tag = "your Category \"" + ((ICategory) q).getName() + "\"";
		else if (q instanceof IMission) tag = "your Task";
		else tag = "";
		
		menu.put(2,
				ItemBuilder.Proto.LIME_WOOL.get().wrapText(
						"&aYes I am sure",
						"",
						"&rThis will delete",
						tag).get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					QuestWorld.getSounds().DESTRUCTIVE_CLICK.playTo(p2);
					// TODO QuestWorld.getSounds().muteNext();
					if (q instanceof ICategory) {
						ICategory category = (ICategory)q;
						if(CancellableEvent.send(new CategoryDeleteEvent(category))) {
							QuestWorld.getFacade().deleteCategory(category);
							p2.closeInventory();
							QuestBook.openCategoryList(p2);
							PlayerTools.sendTranslation(p2, true, Translation.CATEGORY_DELETED, category.getName());
						}
					}
					else if (q instanceof IQuest) {
						IQuest quest = (IQuest)q;
						if(CancellableEvent.send(new QuestDeleteEvent(quest))) {
							ICategoryState changes = quest.getCategory().getState();
							changes.removeQuest(quest);
							if(changes.apply())
								QuestWorld.getFacade().deleteQuest(quest);

							p2.closeInventory();
							QuestBook.openQuestList(p2, quest.getCategory());
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
		
		menu.put(2,
				ItemBuilder.Proto.LIME_WOOL.get().wrapText(
						"&aYes I am sure",
						"",
						"&rThis will reset this Quest's Database").get(),
				event -> {
					q.clearAllUserData();
					QuestBook.openQuestEditor((Player) event.getWhoClicked(), q);
				}
		);
		
		menu.openFor(p);
	}
	
	public static void openQuestMissionEntityEditor(Player p, final IMission mission) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		IMissionState changes = mission.getState();
		//String title = Text.colorize(mission.getQuest().getName() + " &7- &8(Page " + (page+1) + "/" + (lastPage+1) + ")");
		final Menu menu = new Menu(6, "&3Entity selector");
		
		PagedMapping pager = new PagedMapping(45);

		EntityType[] entities = EntityTools.aliveEntityTypes();
		for(int i = 0; i < entities.length; ++i) {
			EntityType entity = entities[i];
			pager.addButton(i,
					EntityTools.getEntityDisplay(entity).wrapText(
							"&7Entity Type: &r" + EntityTools.nameOf(entity),
							"",
							"&e> Click to select").get(),
					event -> {
						changes.setEntity(entity);
						changes.apply();
						QuestBook.openQuestMissionEditor((Player) event.getWhoClicked(), mission);
					}, true
			);
		}
		pager.setBackButton(" &3Mission editor", event -> QuestBook.openQuestMissionEditor(p, mission));
		pager.build(menu, p);
		menu.openFor(p);
	}

	public static void openCommandEditor(Player p, IQuest quest) {
		p.sendMessage(Text.colorize("&7&m----------------------------"));
		for (int i = 0; i < quest.getCommands().size(); i++) {
			String command = quest.getCommands().get(i).replaceAll("(\"|\\\\)", "\\\\$1");
			
			int index = i;
			Prop remove = FUSE(
					HOVER.TEXT("Click to remove this Command", GRAY),
					CLICK.RUN(p, () -> {
						IQuestState changes = quest.getState();
						changes.removeCommand(index);
						if(changes.apply())
							QBDialogue.openCommandEditor(p, quest);
					}));
			
			PlayerTools.tellraw(p, new JsonBlob("X ", DARK_RED, remove)
					.add(command, GRAY, remove).toString());
		}
		
		Prop add = FUSE(
				HOVER.TEXT("Click to add a new Command", GRAY),
				CLICK.RUN(p, () -> {
					PlayerTools.promptCommand(p, new SinglePrompt(
							"&7Type in your desired Command:",
							(c,s) -> {
								IQuestState changes = quest.getState();
								changes.addCommand(s.substring(1));
								if(changes.apply())
									QBDialogue.openCommandEditor(p, quest);
								
								return true;
							}
					));
					p.sendMessage(Text.colorize("&7Usable Variables: @p (Username)"));
					
				}));
		
		PlayerTools.tellraw(p, new JsonBlob("+ ", DARK_GREEN, add)
				.add("Add more Commands... (Click)", GRAY, add).toString());
		
		p.sendMessage(Text.colorize("&7&m----------------------------"));
	}

	public static void openQuestRequirementChooser(Player p, final IStateful quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		Menu menu = new Menu(1, "&3Categories");

		PagedMapping pager = new PagedMapping(45, 9);
		for(ICategory category : QuestWorld.getFacade().getCategories()) {
			pager.addButton(category.getID(), new ItemBuilder(category.getItem()).wrapText(
					category.getName(),
					"",
					"&e> Click to open category").get(),
					event -> {
						Player p2 = (Player)event.getWhoClicked();
						PagedMapping.putPage(p2, 0);
						openQuestRequirementChooser2(p2, quest, category);
					}, true
			);
		}
		
		boolean isQuest = quest instanceof IQuest;
		
		pager.setBackButton(isQuest ? " &3Quest editor" : " &3Category editor", event -> {
			if(isQuest)
				QuestBook.openQuestEditor(p, (IQuest)quest);
			else
				QuestBook.openCategoryEditor(p, (ICategory)quest);
		});
		pager.build(menu, p);
		menu.openFor(p);
	}

	private static void openQuestRequirementChooser2(Player p, final IStateful q, ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		Menu menu = new Menu(1, "&3Quests");
		
		boolean isQuest = q instanceof IQuest;
		String name = isQuest ? ((IQuest)q).getName() : ((ICategory)q).getName();
		
		PagedMapping pager = new PagedMapping(45, 9);
		for(IQuest quest : category.getQuests()) {
			pager.addButton(quest.getID(),
					new ItemBuilder(quest.getItem()).wrapText(
							quest.getName(),
							"",
							"&e> Click to set requirement for " +
							(isQuest ? "quest" : "category") + ": &f&o" + name).get(),
					event -> {
						Player p2 = (Player) event.getWhoClicked();
						PagedMapping.popPage(p2);

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
		pager.setBackButton(" &3Categories", event -> openQuestRequirementChooser(p, q));
		pager.build(menu, p);
		menu.openFor(p);
	}
}
