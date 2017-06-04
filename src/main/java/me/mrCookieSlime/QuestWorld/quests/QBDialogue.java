package me.mrCookieSlime.QuestWorld.quests;

import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuOpeningHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.utils.EntityTools;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
					QuestWorld.getInstance().unregisterCategory((Category) q);
					p.closeInventory();
					QuestBook.openEditor(p);
					PlayerTools.sendTranslation(p, true, Translation.category_deleted, q.getName());
				}
				else if (q instanceof Quest) {
					QuestManager.clearAllQuestData((Quest) q);
					((Quest) q).getCategory().removeQuest((Quest) q);
					p.closeInventory();
					QuestBook.openCategoryQuestEditor(p, ((Quest) q).getCategory());
					PlayerTools.sendTranslation(p, true, Translation.quest_deleted, q.getName());
				}
				else if (q instanceof Mission) {
					((Mission) q).getQuest().removeMission((Mission) q);
					p.closeInventory();
					QuestBook.openQuestEditor(p, ((Mission) q).getQuest());
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
				QuestManager.clearAllQuestData(q);
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
		entities.add(0, EntityType.PLAYER);
		
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
		String title = Text.colorize(mission.getQuest().getName() + " &7- &8(Page " + (page+1) + "/" + (lastPage+1) + ")");
		final ChestMenu menu = new ChestMenu(title);
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		ItemBuilder spawnEgg = new ItemBuilder(Material.MONSTER_EGG).lore("", "&e> Click to select");
		
		int start = page*45;
		int end = start+45;
		for(int index = 0, iter = start; iter < end && iter < entities.size(); ++iter, ++index)
		{
			EntityType entity = entities.get(iter);
			menu.addItem(index, spawnEgg.mob(entity).display("&7Entity Type: &r" + Text.niceName(entity.name())).getNew());
			menu.addMenuClickHandler(index, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					mission.setEntity(entity);
					QuestBook.openQuestMissionEditor(p, mission);
					return false;
				}
			});
		}
		
		int bottomLeftSlot = 45;
		int bottomRightSlot = 53;
		
		ItemBuilder arrow = new ItemBuilder(Material.SKULL_ITEM);
		
		menu.addItem(bottomLeftSlot, arrow.skull("MHF_ArrowLeft").display("&7Prev page").getNew());
		menu.addItem(bottomRightSlot, arrow.skull("MHF_ArrowRight").display("&7Next page").getNew());
		
		MenuClickHandler mh = new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				int delta = 1;
				if(slot == bottomLeftSlot)
					delta = -1;
				
				int nextPage = (page + delta + lastPage + 1) % (lastPage + 1);
				openQuestMissionEntityEditor(p, mission, nextPage, mode);

				return false;
			}
		};
		
		menu.addMenuClickHandler(bottomLeftSlot, mh);
		menu.addMenuClickHandler(bottomRightSlot, mh);

		menu.addItem(49, arrow.skull("MHF_ArrowUp").display("&7Sorting mode").selector(mode, sortingMethods).getNew());
		menu.addMenuClickHandler(49, new MenuClickHandler(){
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				int delta = 1;
				if(action.isRightClicked())
					delta = -1;
				
				int newSort = (mode + delta + sortingMethods.length) % sortingMethods.length;
				openQuestMissionEntityEditor(p, mission, page, newSort);
				
				return false;
			}
			
		});
		
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
		
		for (int i = 0; i < 45; i++) {
			final Category category = QuestWorld.getInstance().getCategory(i);
			List<String> lore = new ArrayList<String>();
			if (category != null) {
				ItemStack item = category.getItem();
				lore.add("");
				lore.add(Text.colorize("&7&oLeft Click to open"));
				ItemMeta im = item.getItemMeta();
				im.setLore(lore);
				item.setItemMeta(im);
				menu.addItem(i, item);
				menu.addMenuClickHandler(i, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						openQuestRequirementChooser2(p, quest, category);
						return false;
					}
				});
			}
		}
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
		
		for (int i = 0; i < 45; i++) {
			final Quest quest = category.getQuest(i);
			List<String> lore = new ArrayList<String>();
			if (quest != null) {
				ItemStack item = quest.getItem();
				lore.add("");
				lore.add(Text.colorize("&7&oClick to select it as a Requirement"));
				lore.add(Text.colorize("&7&ofor the Quest:"));
				lore.add(Text.colorize("&r" + q.getName()));
				ItemMeta im = item.getItemMeta();
				im.setLore(lore);
				item.setItemMeta(im);
				menu.addItem(i, item);
				menu.addMenuClickHandler(i, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						q.setParent(quest);
						if (q instanceof Quest) QuestBook.openQuestEditor(p, (Quest) q);
						else QuestBook.openCategoryEditor(p, (Category) q);
						return false;
					}
				});
			}
		}
		menu.open(p);
	}

}
