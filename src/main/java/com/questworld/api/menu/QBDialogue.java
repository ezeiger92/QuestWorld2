package com.questworld.api.menu;

import static com.questworld.util.json.Prop.*;

import java.util.ArrayDeque;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.questworld.api.QuestWorld;
import com.questworld.api.SinglePrompt;
import com.questworld.api.Translation;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.ICategoryState;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.IQuest;
import com.questworld.api.contract.IQuestState;
import com.questworld.api.contract.IStateful;
import com.questworld.api.event.CancellableEvent;
import com.questworld.api.event.CategoryDeleteEvent;
import com.questworld.api.event.MissionDeleteEvent;
import com.questworld.api.event.QuestDeleteEvent;
import com.questworld.util.EntityTools;
import com.questworld.util.ItemBuilder;
import com.questworld.util.PlayerTools;
import com.questworld.util.Text;
import com.questworld.util.json.JsonBlob;
import com.questworld.util.json.Prop;

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
							if(changes.apply())
								QuestWorld.getFacade().deleteMission(mission);
							
							p2.closeInventory();
							QuestBook.openQuestEditor(p2, mission.getQuest());
							// TODO: Delete mission translation
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
					QuestWorld.getFacade().clearAllUserData(q);
					QuestBook.openQuestEditor((Player) event.getWhoClicked(), q);
				}
		);
		
		menu.openFor(p);
	}
	
	public static void openQuestMissionEntityEditor(Player p, final IMission mission) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		IMissionState changes = mission.getState();
		final Menu menu = new Menu(6, "&3Entity selector");
		
		PagedMapping pager = new PagedMapping(45);

		EntityType[] entities = EntityTools.aliveEntityTypes();
		for(int i = 0; i < entities.length; ++i) {
			EntityType entity = entities[i];
			pager.addButton(i,
					EntityTools.getEntityDisplay(entity).wrapText(
							"&7Entity Type: &e" + EntityTools.nameOf(entity),
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
					HOVER_TEXT("Click to remove this Command", GRAY),
					CLICK_RUN(p, () -> {
						IQuestState changes = quest.getState();
						changes.removeCommand(index);
						if(changes.apply())
							QBDialogue.openCommandEditor(p, quest);
					}));
			
			Prop above = FUSE(
					HOVER_TEXT("Click to insert above", GRAY),
					CLICK_RUN(p, () -> {
						PlayerTools.promptCommand(p, new SinglePrompt(
								"&7Type in your desired Command:",
								(c,s) -> {
									IQuestState changes = quest.getState();
									changes.addCommand(index, s.substring(1));
									if(changes.apply())
										QBDialogue.openCommandEditor(p, quest);
									
									return true;
								}
						));
						p.sendMessage(Text.colorize("&7Usable Variables: @p (Username)"));
						
					}));
			
			PlayerTools.tellraw(p,
					new JsonBlob("^ ", DARK_GREEN, above)
					.add("X ", DARK_RED, remove)
					.add(command, GRAY, remove).toString());
		}
		
		Prop add = FUSE(
				HOVER_TEXT("Click to add a new Command", GRAY),
				CLICK_RUN(p, () -> {
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
		
		Prop back = FUSE(
				HOVER_TEXT("Open quest editor", GRAY),
				CLICK_RUN(p, () -> QuestBook.openQuestEditor(p, quest) ));
		
		PlayerTools.tellraw(p, new JsonBlob("< ", BLUE, back)
				.add("Return to quest editor", GRAY, back).toString());
		
		p.sendMessage(Text.colorize("&7&m----------------------------"));
	}
	
	private static boolean test(ArrayDeque<IQuest> backlog, HashSet<IQuest> collision, IStateful changing) {
		
		HashSet<ICategory> cats = new HashSet<>();
		
		while(backlog.size() > 0) {
			IQuest test = backlog.poll();
			if(test != null) {
				if(test == changing || test.getCategory() == changing)
					return true;
				
				IQuest parent = test.getParent();
				if(parent != null) {
					if(!collision.add(parent))
						return true;
					
					backlog.add(parent);
				}
				
				IQuest catParent = test.getCategory().getParent();
				if(catParent != null && cats.add(test.getCategory())) {
					if(!collision.add(catParent))
						return true;
					
					backlog.add(catParent);
				}
			}
		}
		
		return false;
	}
	
	private static boolean cycleDetection(IStateful changing, IStateful peek) {
		if(changing == peek)
			return true;
		
		HashSet<IQuest> collision = new HashSet<>();
		if(changing instanceof IQuest)
			collision.add((IQuest)changing);
		else
			collision.addAll(((ICategory)changing).getQuests());
		
		ArrayDeque<IQuest> backlog = new ArrayDeque<>();
		IQuest parent = peek instanceof IQuest ? ((IQuest)peek).getParent() : ((ICategory)peek).getParent();
		
		if(parent == null)
			return false;
		
		backlog.add(parent);
		
		return test(backlog, collision, changing);
	}

	public static void openRequirementCategories(Player p, final IStateful quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		Menu menu = new Menu(1, "&3Categories");

		PagedMapping pager = new PagedMapping(45, 9);
		for(ICategory category : QuestWorld.getFacade().getCategories()) {
			if(!cycleDetection(quest, category))
				pager.addButton(category.getID(), new ItemBuilder(category.getItem()).wrapText(
						category.getName(),
						"",
						"&e> Click to open category").get(),
						event -> {
							Player p2 = (Player)event.getWhoClicked();
							PagedMapping.putPage(p2, 0);
							openRequirementQuests(p2, quest, category);
						}, true
				);
			else
				pager.addButton(category.getID(), new ItemBuilder(Material.BARRIER).wrapText(
						category.getName(),
						"",
						"&c> Requirement cycle").get(),
						null, false
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

	private static void openRequirementQuests(Player p, final IStateful q, ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		Menu menu = new Menu(1, "&3Quests");
		
		boolean isQuest = q instanceof IQuest;
		String name = isQuest ? ((IQuest)q).getName() : ((ICategory)q).getName();
		
		PagedMapping pager = new PagedMapping(45, 9);
		for(IQuest quest : category.getQuests()) {
			if(!cycleDetection(q, quest))
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
			else
				pager.addButton(quest.getID(), new ItemBuilder(Material.BARRIER).wrapText(
						quest.getName(),
						"",
						"&c> Requirement cycle").get(),
						null, false
				);
		}
		pager.setBackButton(" &3Categories", event -> openRequirementCategories(p, q));
		pager.build(menu, p);
		menu.openFor(p);
	}
}
