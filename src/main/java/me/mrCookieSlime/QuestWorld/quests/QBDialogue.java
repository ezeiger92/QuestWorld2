package me.mrCookieSlime.QuestWorld.quests;

import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuOpeningHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.audio.Soundboard;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class QBDialogue {
	
	@SuppressWarnings("deprecation")
	public static void openDeletionConfirmation(Player p, final QWObject q) {
		ChestMenu menu = new ChestMenu(Text.colorize("&4&lAre you Sure?"));
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				p.playSound(p.getLocation(), Soundboard.getLegacySounds("NOTE_PLING", "BLOCK_NOTE_HARP"), 1F, 1F);
			}
		});
		
		menu.addItem(6, new CustomItem(new MaterialData(Material.WOOL, (byte) 14), Text.colorize("&cNo")));
		menu.addMenuClickHandler(6, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				if (q instanceof Quest) QuestBook.openCategoryEditor(p, ((Quest) q).getCategory());
				else if (q instanceof Category) QuestBook.openEditor(p);
				else if (q instanceof QuestMission) QuestBook.openQuestEditor(p, ((QuestMission) q).getQuest());
				return false;
			}
		});
		
		String tag = Text.colorize("&r") ;
		if (q instanceof Quest) tag += "your Quest \"" + ((Quest) q).getName() + "\"";
		else if (q instanceof Category) tag += "your Category \"" + ((Category) q).getName() + "\"";
		else if (q instanceof QuestMission) tag += "your Task";
		
		menu.addItem(2, new CustomItem(new MaterialData(Material.WOOL, (byte) 5), Text.colorize("&aYes I am sure"), "", Text.colorize("&rThis will delete"), tag));
		menu.addMenuClickHandler(2, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				if (q instanceof Category) {
					QuestWorld.getInstance().unregisterCategory((Category) q);
					p.closeInventory();
					QuestBook.openEditor(p);
					QuestWorld.getInstance().getLocalization().sendTranslation(p, "editor.deleted-category", true);
				}
				else if (q instanceof Quest) {
					QuestManager.clearAllQuestData((Quest) q);
					((Quest) q).getCategory().removeQuest((Quest) q);
					p.closeInventory();
					QuestBook.openCategoryQuestEditor(p, ((Quest) q).getCategory());
					QuestWorld.getInstance().getLocalization().sendTranslation(p, "editor.deleted-quest", true);
				}
				else if (q instanceof QuestMission) {
					((QuestMission) q).getQuest().removeMission((QuestMission) q);
					QuestBook.openQuestEditor(p, ((QuestMission) q).getQuest());
					p.closeInventory();
					QuestBook.openQuestEditor(p, ((QuestMission) q).getQuest());
				}
				return false;
			}
		});
		
		menu.build().open(p);
	}@SuppressWarnings("deprecation")
	public static void openResetConfirmation(Player p, final Quest q) {
		ChestMenu menu = new ChestMenu(Text.colorize("&4&lAre you Sure?"));
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				p.playSound(p.getLocation(), Soundboard.getLegacySounds("NOTE_PLING", "BLOCK_NOTE_HARP"), 1F, 1F);
			}
		});
		
		menu.addItem(6, new CustomItem(new MaterialData(Material.WOOL, (byte) 14), Text.colorize("&cNo")));
		menu.addMenuClickHandler(6, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				QuestBook.openQuestEditor(p, q);
				return false;
			}
		});
		
		menu.addItem(2, new CustomItem(new MaterialData(Material.WOOL, (byte) 5), Text.colorize("&aYes I am sure"), "", Text.colorize("&rThis will reset this Quest's Database")));
		menu.addMenuClickHandler(2, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				QuestManager.clearAllQuestData(q);
				QuestBook.openQuestEditor(p, q);
				return false;
			}
		});
		
		menu.build().open(p);
	}

	@SuppressWarnings("deprecation")
	public static void openQuestMissionEntityEditor(Player p, final QuestMission mission) {
		final ChestMenu menu = new ChestMenu(mission.getQuest().getName());
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				p.playSound(p.getLocation(), Soundboard.getLegacySounds("UI_BUTTON_CLICK", "CLICK"), 1F, 0.2F);
			}
		});
		
		int[] entities = {50, 51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 120, 53};
		int index = 1;
		
		menu.addItem(0, new CustomItem(new MaterialData(Material.MONSTER_EGG, (byte) -1), "&7Entity Type: &rPLAYER", "", Text.colorize("&e> Click to select")));
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				mission.setEntity(EntityType.PLAYER);
				QuestBook.openQuestMissionEditor(p, mission);
				return false;
			}
		});
		
		for (final int i: entities) {
			try {
				final EntityType entity = EntityType.fromId(i);
				if (entity != null) {
					menu.addItem(index, new CustomItem(new MaterialData(Material.MONSTER_EGG, (byte) i), "&7Entity Type: &r" + entity.toString(), "", Text.colorize("&e> Click to select")));
					menu.addMenuClickHandler(index, new MenuClickHandler() {
						
						@Override
						public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
							mission.setEntity(entity);
							QuestBook.openQuestMissionEditor(p, mission);
							return false;
						}
					});
					index++;
				}
			} catch(Exception x) {
			}
		}
		
		menu.build().open(p);
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

	public static void openQuestRequirementChooser(Player p, final QWObject quest) {
		ChestMenu menu = new ChestMenu("&c&lQuest Editor");
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			@Override
			public void onOpen(Player p) {
				p.playSound(p.getLocation(), Soundboard.getLegacySounds("UI_BUTTON_CLICK", "CLICK"), 1F, 0.2F);
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

	public static void openQuestRequirementChooser2(Player p, final QWObject q, Category category) {
		ChestMenu menu = new ChestMenu("&c&lQuest Editor");
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				p.playSound(p.getLocation(), Soundboard.getLegacySounds("UI_BUTTON_CLICK", "CLICK"), 1F, 0.2F);
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
