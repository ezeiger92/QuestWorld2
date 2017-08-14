package me.mrCookieSlime.QuestWorld.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuOpeningHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.CategoryChange;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestChange;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.menu.Buttons;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.containers.PagedMapping;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import me.mrCookieSlime.QuestWorld.parties.Party;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QuestBook {
	
	public static void openMainMenu(Player p) {
		QuestWorld.getInstance().getManager(p).update(false);
		QuestWorld.getInstance().getManager(p).updateLastEntry(null);
		
		ChestMenu menu = new ChestMenu(QuestWorld.getInstance().getBookLocal("gui.title"));
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().QuestClick().playTo(p);
			}
		});
		
		addPartyMenuButton(menu, p);
		
		PagedMapping view = new PagedMapping(45, 9);
		view.hackNav(4);
		for(Category category : QuestWorld.getInstance().getCategories()) {
			if (!category.isHidden()) {
				if (category.isWorldEnabled(p.getWorld().getName())) {
					if ((category.getParent() != null && !QuestWorld.getInstance().getManager(p).hasFinished(category.getParent())) || !category.hasPermission(p)) {
						view.addItem(category.getID(), new ItemBuilder(Material.BARRIER).display(category.getName()).lore(
								"",
								QuestWorld.getInstance().getBookLocal("quests.locked")).get());
						view.addButton(category.getID(), new MenuClickHandler() {
							
							@Override
							public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
								return false;
							}
						});
					}
					else {
						ItemStack item = category.getItem();
						ItemMeta im = item.getItemMeta();
						List<String> lore = new ArrayList<String>();
						lore.add("");
						lore.add(category.getProgress(p));
						lore.add("");
						lore.add(Text.colorize("&7" + category.getQuests().size() + QuestWorld.getInstance().getBookLocal("category.desc.total")));
						lore.add(Text.colorize("&a" + category.getFinishedQuests(p).size() + QuestWorld.getInstance().getBookLocal("category.desc.completed")));
						lore.add(Text.colorize("&b" + category.getQuests(p, QuestStatus.AVAILABLE).size() + QuestWorld.getInstance().getBookLocal("category.desc.available")));
						lore.add(Text.colorize("&e" + category.getQuests(p, QuestStatus.ON_COOLDOWN).size() + QuestWorld.getInstance().getBookLocal("category.desc.cooldown")));
						lore.add(Text.colorize("&5" + category.getQuests(p, QuestStatus.REWARD_CLAIMABLE).size() + QuestWorld.getInstance().getBookLocal("category.desc.claimable_reward")));
						im.setLore(lore);
						item.setItemMeta(im);
						view.addItem(category.getID(), item);
						view.addNavButton(category.getID(), new MenuClickHandler() {
							
							@Override
							public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
								QuestWorld.getInstance().getManager(p).putPage(0);
								openCategory(p, category, true);
								return false;
							}
						});
					}
				}
				else {
					view.addItem(category.getID(), new ItemBuilder(Material.BARRIER).display(category.getName()).lore(
							"",
							QuestWorld.getInstance().getBookLocal("quests.locked-in-world")).get());
					view.addButton(category.getID(), new MenuClickHandler() {
						
						@Override
						public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
							return false;
						}
					});
				}
			}
		}
		view.build(menu, p);
		menu.open(p);
	}
	
	public static void openLastMenu(Player p) {
		QuestingObject last = QuestWorld.getInstance().getManager(p).getLastEntry();
		if (last != null) {			
			if(last instanceof Quest) {
				Quest q = (Quest)last;
				
				if(q.isValid()) {
					QuestBook.openQuest(p, q, true, true);
					return;
				}
				else
					last = q.getCategory();
			}
			
			if (last instanceof Category) {
				Category c = (Category)last;

				if(c.isValid()) {
					QuestBook.openCategory(p, c, true);
					return;
				}
			}
		}
		
		QuestBook.openMainMenu(p);
	}
	
	private static void addPartyMenuButton(ChestMenu menu, Player p) {
		if (QuestWorld.getInstance().getCfg().getBoolean("party.enabled")) {
			ItemStack skullItem = new ItemBuilder(SkullType.PLAYER)
				.display(QuestWorld.getInstance().getBookLocal("gui.party"))
				.lore(QuestWorld.getInstance().getManager(p).getProgress(), "", QuestWorld.getInstance().getBookLocal("button.open"))
				.get();
			menu.addItem(4, skullItem);
			menu.addMenuClickHandler(4, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					openPartyMenu(arg0);
					return false;
				}
			});
		}
		else {
			menu.addItem(4, new ItemBuilder(Material.ENCHANTED_BOOK)
					.display("&eQuest Book")
					.lore("", QuestWorld.getInstance().getManager(p).getProgress()).get());
			menu.addMenuClickHandler(4, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					return false;
				}
			});
		}
	}

	public static void openPartyMembers(final Player p) {
		ChestMenu menu = new ChestMenu(QuestWorld.getInstance().getBookLocal("gui.party"));
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().PartyClick().playTo(p);
			}
		});
		ItemBuilder skull = new ItemBuilder(SkullType.PLAYER);
		menu.addItem(4, skull.display(QuestWorld.getInstance().getBookLocal("gui.party")).lore("", QuestWorld.getInstance().getBookLocal("button.back.party")).get());
		menu.addMenuClickHandler(4, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
				openPartyMenu(arg0);
				return false;
			}
		});
		
		final Party party = QuestWorld.getInstance().getManager(p).getParty();
		if (party != null) {
			for (int i = 0; i < party.getSize(); i++) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(party.getPlayers().get(i));
				if (!party.isLeader(p)) {
					
					ItemStack item = skull.skull(player.getName()).display("&e" + player.getName()).lore("", (party.isLeader(player) ? "&4Party Leader": "&eParty Member")).get();
					menu.addItem(i + 9, item);
					menu.addMenuClickHandler(i + 9, new MenuClickHandler() {
						
						@Override
						public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
							return false;
						}
					});
				}
				else {
					ItemStack item = skull.skull(player.getName())
							.display("&e" + player.getName())
							.lore("", (party.isLeader(player) ? "&5&lParty Leader": "&e&lParty Member"), "", (party.isLeader(player) ? "": "&7&oClick here to kick this Member"))
							.get();
					menu.addItem(i + 9, item);
					menu.addMenuClickHandler(i + 9, new MenuClickHandler() {
						
						@Override
						public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
							if (!party.isLeader(player)) {
								party.kickPlayer(player.getName());
								openPartyMembers(p);
							}
							return false;
						}
					});
				}
			}
		}
		
		menu.open(p);
	}

	public static void openPartyMenu(final Player p) {
		ChestMenu menu = new ChestMenu(QuestWorld.getInstance().getBookLocal("gui.party"));
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().PartyClick().playTo(p);
			}
		});
		
		menu.addItem(4, new ItemBuilder(Material.MAP).display(QuestWorld.getInstance().getBookLocal("gui.title")).lore(
				"",
				QuestWorld.getInstance().getBookLocal("button.back.quests")).get());
		menu.addMenuClickHandler(4, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
				openMainMenu(p);
				return false;
			}
		});
		
		final Party party = QuestWorld.getInstance().getManager(p).getParty();
		
		ItemBuilder wool = new ItemBuilder(Material.WOOL);
		
		if (party == null) {
			menu.addItem(9, wool.color(DyeColor.GREEN).display("&a&lCreate a new Party").lore("", "&rCreates a brand new Party for you", "&rto invite Friends and share your Progress").getNew());
			menu.addMenuClickHandler(9, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					Party.create(p);
					openPartyMenu(p);
					return false;
				}
			});
		}
		else {
			if (party.isLeader(p)) {
				menu.addItem(9, wool.color(DyeColor.GREEN).display("&a&lInvite a Player").lore("", "&rInvites a Player to your Party", "&rMax. Party Members: &e" + QuestWorld.getInstance().getCfg().getInt("party.max-members")).getNew());
				menu.addMenuClickHandler(9, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						if (party.getSize() >= QuestWorld.getInstance().getCfg().getInt("party.max-members"))
							PlayerTools.sendTranslation(p, true, Translation.party_errorfull);
						else {
							PlayerTools.promptInput(p, new SinglePrompt(
									PlayerTools.makeTranslation(true, Translation.party_playerpick),
									(c,s) -> {
										String name = Text.decolor(s).replace("@", "");

										Player player = PlayerTools.getPlayer(name);
										if (player != null) {
											if (QuestWorld.getInstance().getManager(player).getParty() == null) {
												PlayerTools.sendTranslation(p, true, Translation.party_playeradd, name);
												try {
													party.invitePlayer(player);
												} catch (Exception e1) {
													e1.printStackTrace();
												}
											}
											else PlayerTools.sendTranslation(p, true, Translation.party_errormember, name);
										}
										else {
											PlayerTools.sendTranslation(p, true, Translation.party_errorabsent, name);
										}
										return true;
									}
							));
							
							p.closeInventory();
						}
						return false;
					}
				});
				
				menu.addItem(17, wool.color(DyeColor.RED).display("&4&lDelete your Party").lore("", "&rDeletes this Party", "&rBe careful with this Option!").getNew());
				menu.addMenuClickHandler(17, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						party.abandon();
						openPartyMenu(p);
						return false;
					}
				});
			}
			else {
				menu.addItem(17, wool.color(DyeColor.RED).display("&4&lLeave your Party").lore("", "&rLeaves this Party", "&rBe careful with this Option!").getNew());
				menu.addMenuClickHandler(17, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						party.kickPlayer(p.getName());
						openPartyMenu(p);
						return false;
					}
				});
			}
			ItemStack skullItem = new ItemBuilder(SkullType.PLAYER).display("&eMember List").lore("", "&rShows you all Members of this Party").get();
			menu.addItem(13, skullItem);
			menu.addMenuClickHandler(13, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					openPartyMembers(p);
					return false;
				}
			});
		}
		
		menu.open(p);
	}

	public static void openCategory(Player p, Category category, final boolean back) {
		QuestWorld.getInstance().getManager(p).update(false);
		QuestWorld.getInstance().getManager(p).updateLastEntry(category);
		
		ChestMenu menu = new ChestMenu(QuestWorld.getInstance().getBookLocal("gui.title"));
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().QuestClick().playTo(p);
			}
		});
		
		addPartyMenuButton(menu, p);
		
		if (back) {
			menu.addItem(0, new ItemBuilder(Material.MAP).display(QuestWorld.getInstance().getBookLocal("button.back.general")).get());
			menu.addMenuClickHandler(0, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					openMainMenu(arg0);
					return false;
				}
			});
		}
		
		ItemBuilder glassPane = new ItemBuilder(Material.STAINED_GLASS_PANE).color(DyeColor.RED);
		
		PagedMapping view = new PagedMapping(45, 9);
		view.hackNav(4);
		for (final Quest quest: category.getQuests()) {
			glassPane.display(quest.getName());
			if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.LOCKED) || !quest.isWorldEnabled(p.getWorld().getName())) {
				view.addItem(quest.getID(), glassPane.lore("", QuestWorld.getInstance().getBookLocal("quests.locked")).getNew());
				view.addButton(quest.getID(), new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						return false;
					}
				});
			}
			else if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.LOCKED_NO_PARTY)) {
				view.addItem(quest.getID(), glassPane.lore("", "&4You need to leave your current Party").getNew());
				view.addButton(quest.getID(), new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						return false;
					}
				});
			}
			else if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.LOCKED_PARTY_SIZE)) {
				view.addItem(quest.getID(), glassPane.lore("", "&4You can only do this Quest in a Party", "&4with at least &c" + quest.getPartySize() + " &4Members").getNew());
				view.addButton(quest.getID(), new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						return false;
					}
				});
			}
			else {
				ItemStack item = quest.getItem();
				ItemMeta im = item.getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.add("");
				lore.add(quest.getProgress(p));
				lore.add("");
				lore.add(Text.colorize("&7") + quest.getFinishedTasks(p).size() + "/" + quest.getMissions().size() + QuestWorld.getInstance().getBookLocal("quests.tasks_completed"));
				if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.REWARD_CLAIMABLE)) {
					lore.add("");
					lore.add(QuestWorld.getInstance().getBookLocal("quests.state.reward_claimable"));
				}
				else if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
					lore.add("");
					lore.add(QuestWorld.getInstance().getBookLocal("quests.state.cooldown"));
				}
				else if (QuestWorld.getInstance().getManager(p).hasFinished(quest)) {
					lore.add("");
					lore.add(QuestWorld.getInstance().getBookLocal("quests.state.completed"));
				}
				im.setLore(lore);
				item.setItemMeta(im);
				view.addItem(quest.getID(), item);
				view.addNavButton(quest.getID(), new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						openQuest(p, quest, back, true);
						return false;
					}
				});
			}
		}
		view.build(menu, p);
		menu.open(p);
	}
	
	public static void openQuest(final Player p, final Quest quest, final boolean categoryBack, final boolean back) {
		QuestWorld.getInstance().getManager(p).update(false);
		QuestWorld.getInstance().getManager(p).updateLastEntry(quest);
		
		ChestMenu menu = new ChestMenu(QuestWorld.getInstance().getBookLocal("gui.title"));
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().QuestClick().playTo(p);
			}
		});
		
		if (back) {
			menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem());
			menu.addMenuClickHandler(0, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					openCategory(p, quest.getCategory(), categoryBack);
					return false;
				}
			});
		}
		
		// Detect all
		menu.addItem(1, new ItemBuilder(Material.CHEST).display("&7Check all Tasks").get());
		menu.addMenuClickHandler(1, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
				PlayerManager manager = QuestWorld.getInstance().getManager(p);
				for(Mission mission : quest.getMissions()) {
					if (!manager.hasUnlockedTask(mission)) continue;
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						if (manager.hasCompletedTask(mission)) continue;
						
						if(mission.getType() instanceof Manual) {
							Manual m = (Manual) mission.getType();
							int progress = m.onManual(p, mission);
							if(progress != Manual.FAIL) {
								manager.setProgress(mission, progress);
								openQuest(p, quest, categoryBack, back);
							}
						}
					}
				}
				return false;
			}
		});
		
		if (quest.getCooldown() >= 0) {
			String cooldown = quest.getFormattedCooldown();
			if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
				long remaining = (QuestWorld.getInstance().getManager(p).getCooldownEnd(quest) - System.currentTimeMillis() + 59999) / 60 / 1000;
				cooldown = (remaining / 60) + "h " + (remaining % 60) + "m remaining";
			}
			menu.addItem(8, new ItemBuilder(Material.WATCH).display(QuestWorld.getInstance().getBookLocal("quests.display.cooldown")).lore(
					"",
					"&b" + cooldown).get());
			menu.addMenuClickHandler(8, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					return false;
				}
			});
		}
		
		int rewardIndex = 2;
		if (quest.getMoney() > 0 && QuestWorld.getInstance().getEconomy() != null) {
			menu.addItem(rewardIndex, new ItemBuilder(Material.GOLD_INGOT).display(QuestWorld.getInstance().getBookLocal("quests.display.monetary")).lore(
					"",
					"&6$" + quest.getMoney()).get());
			menu.addMenuClickHandler(rewardIndex, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					return false;
				}
			});
			rewardIndex++;
		}
		
		if (quest.getXP() > 0) {
			menu.addItem(rewardIndex, new ItemBuilder(Material.EXP_BOTTLE).display(QuestWorld.getInstance().getBookLocal("quests.display.exp")).lore(
					"",
					"&a" + quest.getXP() + " Level").get());
			menu.addMenuClickHandler(rewardIndex, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					return false;
				}
			});
			rewardIndex++;
		}
		
		ItemBuilder glassPane = new ItemBuilder(Material.STAINED_GLASS_PANE);
		
		int index = 9;
		for (final Mission mission: quest.getMissions()) {
			if (QuestWorld.getInstance().getManager(p).hasUnlockedTask(mission)) {

				ItemBuilder entryItem = new ItemBuilder(mission.getDisplayItem()).display(mission.getText());
				
				if(mission.getType() instanceof Manual) {
					String label = ((Manual) mission.getType()).getLabel();
					entryItem.lore("", mission.getProgress(p), "", "&r> Click for Manual " + label);
				}
				else
					entryItem.lore("", mission.getProgress(p));

				menu.addItem(index, entryItem.get());
			}
			else {
				
				menu.addItem(index, glassPane.color(DyeColor.RED).display("&7&kSOMEWEIRDMISSION").lore("", QuestWorld.getInstance().getBookLocal("task.locked")).get());
			}
			
			menu.addMenuClickHandler(index, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					PlayerManager manager = QuestWorld.getInstance().getManager(p);
					
					if (!manager.hasUnlockedTask(mission)) return false;
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						if (manager.hasCompletedTask(mission)) return false;
						
						if(mission.getType() instanceof Manual) {
							Manual m = (Manual) mission.getType();
							int progress = m.onManual(p, mission);
							if(progress != Manual.FAIL) {
								manager.setProgress(mission, progress);
								openQuest(p, quest, categoryBack, back);
							}
						}
					}
					return false;
				}
			});
			index++;
		}
		
		for (int i = 0; i < 9; i++) {
			if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.REWARD_CLAIMABLE)) {
				menu.addItem(i + 18, glassPane.color(DyeColor.PURPLE).display(QuestWorld.getInstance().getBookLocal("quests.state.reward_claim")).get());
				menu.addMenuClickHandler(i + 18, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						quest.handoutReward(p);
						QuestWorld.getSounds().muteNext();
						openQuest(p, quest, categoryBack, back);
						return false;
					}
				});
			}
			else if (QuestWorld.getInstance().getManager(p).getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
				menu.addItem(i + 18, glassPane.color(DyeColor.YELLOW).display(QuestWorld.getInstance().getBookLocal("quests.state.cooldown")).get());
				menu.addMenuClickHandler(i + 18, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						return false;
					}
				});
			}
			else {
				menu.addItem(i + 18, glassPane.color(DyeColor.GRAY).display(QuestWorld.getInstance().getBookLocal("quests.display.rewards")).get());
				menu.addMenuClickHandler(i + 18, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						return false;
					}
				});
			}
		}
		
		int slot = 27;
		for (ItemStack reward: quest.getRewards()) {
			menu.addItem(slot, reward);
			menu.addMenuClickHandler(slot, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					return false;
				}
			});
			slot++;
		}
		
		menu.open(p);
	}

	
	/*
	 * 
	 * 			Quest Editor
	 * 
	 */
	public static void openEditor(Player p) {
		final ChestMenu menu = new ChestMenu("§3Quest Editor");
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		ItemBuilder defaultItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
				.color(DyeColor.RED).display("&7&o> New Category");

		PagedMapping view = new PagedMapping(45);
		view.touch(0); // Dummy, force a page to exist
		
		int found = 0, categoryCount = QuestWorld.getInstance().getCategories().size();
		for(int i = 0; i < view.getCapacity(); ++i) {
			if(found < categoryCount)
				view.touch(i + view.getPageCapacity());
			
			Category category = QuestWorld.getInstance().getCategory(i);
			if(category != null) {
				String[] lore = {
						"",
						"&c&oLeft Click to edit",
						"&c&oShift + Left Click to open",
						"&c&oRight Click to delete"
				};
				int quests = category.getQuests().size();
				if(quests > 0) {
					int j = 0;
					List<String> lines = new ArrayList<>();
					for(Quest q : category.getQuests()) {
						lines.add("&7- " + q.getName());
						if(++j >= 5)
							break;
					}
					if(j < quests)
						lines.add("&7&oand "+(quests-j)+" more...");
					String[] newLore = lines.toArray(new String[lines.size() + lore.length]);
					for(j = 0; j < lore.length; ++j)
						newLore[lines.size() + j] = lore[j];
					lore = newLore;
				}
				
				++found;
				view.addItem(i, new ItemBuilder(category.getItem()).lore(lore).get());
				view.addNavButton(i, Buttons.onCategory(category));
			}
			else {
				view.addItem(i, defaultItem.get());
				view.addNavButton(i, Buttons.newCategory(i));
			}
		}

		view.build(menu, p);
		menu.open(p);
	}

	public static void openCategoryQuestEditor(Player p, final Category category) {
		final ChestMenu menu = new ChestMenu("§3Quest Editor");
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem());
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openEditor(p);
				return false;
			}
		});
		
		ItemBuilder defaultItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
				.color(DyeColor.RED).display("&7&o> New Quest");
		
		PagedMapping view = new PagedMapping(45);
		view.touch(0); // Dummy, force a page to exist
		
		int found = 0, questCount = category.getQuests().size();
		for (int i = 0; i < view.getCapacity(); ++i) {
			if(found < questCount)
				view.touch(i + view.getPageCapacity());
			
			Quest quest = category.getQuest(i);
			if (quest != null) {
				int missions = quest.getMissions().size();
				String[] lore = {
					"",
					"&c&oLeft Click to edit",
					"&c&oRight Click to delete"
				};
				
				if(missions > 0) {
					int j = 0;
					List<String> lines = new ArrayList<>();
					for(Mission m : quest.getMissions()) {
						lines.add("&7- " + m.getText());
						if(++j >= 5)
							break;
					}
					if(j < missions)
						lines.add("&7&oand "+(missions-j)+" more...");
					String[] newLore = lines.toArray(new String[lines.size() + lore.length]);
					for(j = 0; j < lore.length; ++j)
						newLore[lines.size() + j] = lore[j];
					lore = newLore;
				}
				
				++found;
				view.addItem(i, new ItemBuilder(quest.getItem()).lore(lore).get());
				view.addNavButton(i, Buttons.onQuest(quest));
			}
			else {
				view.addItem(i, defaultItem.getNew());
				view.addNavButton(i, Buttons.newQuest(category.getID(), i));
			}
		}
		view.build(menu, p);
		menu.open(p);
	}

	public static void openCategoryEditor(Player p, final Category category) {
		final ChestMenu menu = new ChestMenu("§3Quest Editor");
		CategoryChange changes = new CategoryChange(category);
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem());
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openEditor(p);
				return false;
			}
		});
		
		ItemStack item = category.getItem().clone();
		ItemMeta im = item.getItemMeta();
		im.setLore(Arrays.asList("", "§e> Click to change the Item to", "§ethe Item you are currently holding"));
		item.setItemMeta(im);
		
		menu.addItem(9, item);
		menu.addMenuClickHandler(9, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				ItemStack hand = PlayerTools.getActiveHandItem(p);
				if (hand != null) {
					changes.setItem(hand);
					if(changes.sendEvent())
						changes.apply();
					openCategoryEditor(p, category);
				}
				return false;
			}
		});
		
		menu.addItem(10, new ItemBuilder(Material.NAME_TAG).display(category.getName()).lore("", "§e> Click to change the Name").get());
		menu.addMenuClickHandler(10, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				PlayerTools.promptInput(p, new SinglePrompt(
						PlayerTools.makeTranslation(true, Translation.category_namechange, category.getName()),
						(c,s) -> {
							CategoryChange changes = new CategoryChange(category);
							changes.setName(s);
							if(changes.sendEvent()) {
								String oldName = category.getName();
								changes.apply();
								PlayerTools.sendTranslation(p, true, Translation.category_nameset, s, oldName);
							}
							
							QuestBook.openCategoryEditor(p, category);
							return true;
						}
				));

				p.closeInventory();
				return false;
			}
		});
		
		menu.addItem(11, new ItemBuilder(Material.BOOK_AND_QUILL).display("&7Quest Requirement:").lore(
				"",
				(category.getParent() != null ? "§r" + category.getParent().getName(): "§7§oNone"),
				"",
				"§rLeft Click: §eChange Quest Requirement",
				"§rRight Click: §eRemove Quest Requirement").get());
		menu.addMenuClickHandler(11, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				if (action.isRightClicked()) {
					changes.setParent(null);
					if(changes.sendEvent())
						changes.apply();
					openCategoryEditor(p, category);
				}
				else {
					QuestWorld.getInstance().getManager(p).putPage(0);
					QBDialogue.openQuestRequirementChooser(p, category);
				}
				return false;
			}
		});
		
		menu.addItem(12, new ItemBuilder(Material.NAME_TAG)
				.display("&r" + (category.getPermission().equals("") ? "None": category.getPermission())).lore(
				"",
				"&e> Click to change the rquired Permission Node").get());
		menu.addMenuClickHandler(12, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				PlayerTools.promptInput(p, new SinglePrompt(
						PlayerTools.makeTranslation(true, Translation.category_permchange, category.getName(), category.getPermission()),
						(c,s) -> {
							CategoryChange changes = new CategoryChange(category);
							String permission = s.equalsIgnoreCase("none") ? "": s;
							changes.setPermission(permission);
							if(changes.sendEvent()) {
								String oldPerm = category.getPermission();
								changes.apply();
								PlayerTools.sendTranslation(p, true, Translation.category_permset, category.getName(), s, oldPerm);
							}
							
							QuestBook.openCategoryEditor(p, category);
							return true;
						}
				));
				
				p.closeInventory();
				return false;
			}
		});
		
		menu.addItem(13, new ItemBuilder(Material.GOLDEN_CARROT).display("§rShow in Quest Book: " + (!category.isHidden() ? "&2&l\u2714": "&4&l\u2718")).lore(
				"",
				"§e> Click to change whether this Category",
				"&ewill appear in the Quest Book").get());
		menu.addMenuClickHandler(13, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				changes.setHidden(!category.isHidden());
				if(changes.sendEvent())
					changes.apply();
				openCategoryEditor(p, category);
				return false;
			}
		});
		
		menu.addItem(14, new ItemBuilder(Material.GRASS).display("&7World Blacklist").lore(
				"",
				"&e> Click to configure in which Worlds",
				"&ethis Category is enabled").get());
		menu.addMenuClickHandler(14, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openWorldEditor(p, category);
				return false;
			}
		});
		
		menu.addItem(17, ItemBuilder.Proto.RED_WOOL.get().display("&4Delete Database").lore(
				"",
				"&rThis is going to delete the Database",
				"&rof all Quests inside this Category",
				"&rand will clear all Player's Progress associated",
				"&rwith those Quests.").get());
		menu.addMenuClickHandler(17, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				for (Quest quest: category.getQuests()) {
					PlayerManager.clearAllQuestData(quest);
				}
				QuestWorld.getSounds().DestructiveClick().playTo(p);
				return false;
			}
		});
		
		menu.open(p);
	}

	public static void openQuestEditor(Player p, final Quest quest) {
		final ChestMenu menu = new ChestMenu("§3Quest Editor");
		QuestChange changes = new QuestChange(quest);
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem());
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openCategoryQuestEditor(p, quest.getCategory());
				return false;
			}
		});
		
		ItemStack item = new ItemBuilder(quest.getItem()).lore(
				"",
				"&e> Click to change the Item to",
				"&ethe Item you are currently holding").get();

		
		menu.addItem(9, item);
		menu.addMenuClickHandler(9, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				ItemStack mainItem = p.getInventory().getItemInMainHand();
				if (mainItem != null) {
					changes.setItem(mainItem);
					if(changes.sendEvent())
						changes.apply();
					
					openQuestEditor(p, quest);
				}
				return false;
			}
		});
		
		menu.addItem(10, new ItemBuilder(Material.NAME_TAG).display(quest.getName()).lore("", "&e> Click to change the Name").get());
		menu.addMenuClickHandler(10, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				PlayerTools.promptInput(p, new SinglePrompt(
						PlayerTools.makeTranslation(true, Translation.quest_namechange, quest.getName()),
						(c,s) -> {
							QuestChange changes = new QuestChange(quest);
							changes.setName(s);
							if(changes.sendEvent()) {
								String oldName = quest.getName();
								changes.apply();
								PlayerTools.sendTranslation(p, true, Translation.quest_nameset, s, oldName);
							}

							QuestBook.openQuestEditor(p, quest);
							return true;
						}
				));
				
				p.closeInventory();
				return false;
			}
		});
		
		menu.addItem(11, new ItemBuilder(Material.CHEST).display("&rRewards &7(Item)").lore(
				"",
				"&e> Click to change the Rewards",
				"&eto be the Items in your Hotbar").get());
		menu.addMenuClickHandler(11, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				changes.setItemRewards(p);
				if(changes.sendEvent())
					changes.apply();
				
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		menu.addItem(12, new ItemBuilder(Material.WATCH).display("&7Cooldown: &b" + quest.getFormattedCooldown()).lore(
				"",
				"&rLeft Click: &e+1m",
				"&rRight Click: &e-1m",
				"&rShift + Left Click: &e+1h",
				"&rShift + Right Click: &e-1h").get());
		menu.addMenuClickHandler(12, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				long cooldown = quest.getCooldown();
				long delta = action.isShiftClicked() ? 60: 1;
				if (action.isRightClicked()) delta = -delta;

				// Force a step at 0, so you can't jump from 59 -> -1 or -1 -> 59
				if(cooldown + delta < 0) {
					if(cooldown <= 0) 
						cooldown = -1;
					else
						cooldown = 0;
				}
				else if(cooldown == -1)
					cooldown = 0;
				else
					cooldown += delta;
				
				changes.setCooldown(cooldown);
				if(changes.sendEvent())
					changes.apply();
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		if (QuestWorld.getInstance().getEconomy() != null) {
			menu.addItem(13, new ItemBuilder(Material.GOLD_INGOT).display("&7Monetary Reward: &6$" + quest.getMoney()).lore(
					"",
					"&rLeft Click: &e+1",
					"&rRight Click: &e-1",
					"&rShift + Left Click: &e+100",
					"&rShift + Right Click: &e-100").get());
			menu.addMenuClickHandler(13, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					int money = quest.getMoney();
					if (action.isRightClicked()) money = money - (action.isShiftClicked() ? 100: 1);
					else money = money + (action.isShiftClicked() ? 100: 1);
					if (money < 0) money = 0;
					changes.setMoney(money);
					if(changes.sendEvent())
						changes.apply();
					openQuestEditor(p, quest);
					return false;
				}
			});
		}
		
		menu.addItem(14, new ItemBuilder(Material.EXP_BOTTLE).display("&7XP Reward: &b" + quest.getXP() + " Level").lore(
				"",
				"&rLeft Click: &e+1",
				"&rRight Click: &e-1",
				"&rShift + Left Click: &e+10",
				"&rShift + Right Click: &e-10").get());
		menu.addMenuClickHandler(14, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				int xp = quest.getXP();
				if (action.isRightClicked()) xp = xp - (action.isShiftClicked() ? 10: 1);
				else xp = xp + (action.isShiftClicked() ? 10: 1);
				if (xp < 0) xp = 0;
				changes.setXP(xp);
				if(changes.sendEvent())
					changes.apply();
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		menu.addItem(15, new ItemBuilder(Material.BOOK_AND_QUILL).display("&7Quest Requirement:").lore(
				"",
				(quest.getParent() != null ? "&r" + quest.getParent().getName(): "&7&oNone"),
				"",
				"&rLeft Click: &eChange Quest Requirement",
				"&rRight Click: &eRemove Quest Requirement").get());
		menu.addMenuClickHandler(15, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				if (action.isRightClicked()) {
					changes.setParent(null);
					if(changes.sendEvent())
						changes.apply();
					openQuestEditor(p, quest);
				}
				else {
					QuestWorld.getInstance().getManager(p).putPage(0);
					QBDialogue.openQuestRequirementChooser(p, quest);
				}
				return false;
			}
		});
		
		menu.addItem(16, new ItemBuilder(Material.COMMAND).display("&7Commands executed upon Completion").lore(
				"",
				"&rLeft Click: &eOpen Command Editor").get());
		menu.addMenuClickHandler(16, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				p.closeInventory();
				QBDialogue.openCommandEditor(p, quest);
				return false;
			}
		});
		
		menu.addItem(17, new ItemBuilder(Material.NAME_TAG)
				.display("&r" + (quest.getPermission().equals("") ? "None": quest.getPermission())).lore(
				"",
				"&e> Click to change the required Permission Node").get());
		menu.addMenuClickHandler(17, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				PlayerTools.promptInput(p, new SinglePrompt(
						PlayerTools.makeTranslation(true, Translation.quest_permchange, quest.getName(), quest.getPermission()),
						(c,s) -> {
							QuestChange changes = new QuestChange(quest);
							String permission = s.equalsIgnoreCase("none") ? "": s;
							changes.setPermission(permission);
							if(changes.sendEvent()) {
								String oldPerm = quest.getPermission();
								changes.apply();
								PlayerTools.sendTranslation(p, true, Translation.quest_permset, quest.getName(), s, oldPerm);
							}

							QuestBook.openQuestEditor(p, quest);
							return true;
						}
				));
				
				p.closeInventory();
				return false;
			}
		});
		
		menu.addItem(18, new ItemBuilder(Material.FIREWORK).display("&rParty Support: " + (quest.supportsParties() ? "&2&l\u2714": "&4&l\u2718")).lore(
				"",
				"§e> Click to change whether this Quest can be done in Parties or not").get());
		menu.addMenuClickHandler(18, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				changes.setPartySupport(quest.supportsParties());
				if(changes.sendEvent())
					changes.apply();
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		menu.addItem(19, new ItemBuilder(Material.COMMAND).display("&rOrdered Completion Mode: " + (quest.isOrdered() ? "&2&l\u2714": "&4&l\u2718")).lore(
				"",
				"&e> Click to change whether this Quest's Tasks",
				"&ehave to be done in the Order they are arranged").get());
		menu.addMenuClickHandler(19, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				changes.setOrdered(!quest.isOrdered());
				if(changes.sendEvent())
					changes.apply();
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		menu.addItem(20, new ItemBuilder(Material.CHEST).display("&rAuto-Claim Rewards: " + (quest.isAutoClaiming() ? "&2&l\u2714": "&4&l\u2718")).lore(
				"",
				"&e> Click to change whether this Quest's Rewards",
				"&ewill be automatically given or have to be",
				"&eclaimed manually").get());
		menu.addMenuClickHandler(20, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				changes.setAutoClaim(!changes.isAutoClaiming());
				if(changes.sendEvent())
					changes.apply();
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		menu.addItem(21, new ItemBuilder(Material.GRASS).display("&7World Blacklist").lore(
				"",
				"&e> Click to configure in which Worlds",
				"&ethis Quest is able to be completed").get());
		menu.addMenuClickHandler(21, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openWorldEditor(p, quest);
				return false;
			}
		});
		String wtfString = "&rMinimal Party Size: " + (quest.getPartySize() < 1 ? "&4Players aren't allowed be in a Party": (quest.getPartySize() == 1 ? ("&ePlayers can but don't have to be in a Party") : ("&aPlayers need to be in a Party of " + quest.getPartySize() + " or more")));
		menu.addItem(22, new ItemBuilder(Material.FIREWORK).display(wtfString).lore(
				"",
				"&eChange the min. Amount of Players in",
				"&ea Party needed to start this Quest",
				"",
				"&r1 = &7Players can but don't have to be in a Party",
				"&r0 = &7Players aren't allowed to be in a Party",
				"",
				"&rLeft Click: &e+1",
				"&rRight Click: &e-1").get());
		menu.addMenuClickHandler(22, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				int size = quest.getPartySize();
				if (action.isRightClicked()) size--;
				else size++;
				if (size < 0) size = 0;
				changes.setPartySize(size);
				if(changes.sendEvent())
					changes.apply();
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		menu.addItem(26, ItemBuilder.Proto.RED_WOOL.get().display("&4Delete Database").lore(
				"",
				"&rThis is going to delete this Quest's Database",
				"&rand will clear all Player's Progress associated",
				"&rwith this Quest.").get());
		menu.addMenuClickHandler(26, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				PlayerManager.clearAllQuestData(quest);
				QuestWorld.getSounds().DestructiveClick().playTo(p);
				return false;
			}
		});
		
		int index = 36;
		for (ItemStack reward: quest.getRewards()) {
			menu.addItem(index, reward);
			menu.addMenuClickHandler(index, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
					return false;
				}
			});
			index++;
		}
		
		for (int i = 0; i < 9; i++) {
			final Mission mission = quest.getMission(i);
			if (mission == null) {
				menu.addItem(45 + i, new ItemBuilder(Material.PAPER).display("&7&o> New Task").get());
				menu.addMenuClickHandler(45 + i, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						changes.addMission(new Mission(quest, String.valueOf(slot - 36), MissionType.valueOf("SUBMIT"), EntityType.PLAYER, "", new ItemStack(Material.STONE), p.getLocation().getBlock().getLocation(), 1, null, 0, false, 0, true, "Hey there! Do this Quest."));
						if(changes.sendEvent())
							changes.apply();
						openQuestEditor(p, quest);
						return false;
					}
				});
			}
			else {
				ItemStack stack = new ItemBuilder(Material.BOOK).display(mission.getText()).lore(
						"",
						"&c&oLeft Click to edit",
						"&c&oRight Click to delete").get();

				menu.addItem(45 + i, stack);
				menu.addMenuClickHandler(45 + i, new MenuClickHandler() {
					
					@Override
					public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
						if (action.isRightClicked()) QBDialogue.openDeletionConfirmation(p, mission);
						else openQuestMissionEditor(p, mission);
						return false;
					}
				});
			}
		}
		
		menu.open(p);
	}

	public static void openWorldEditor(Player p, final Quest quest) {
		final ChestMenu menu = new ChestMenu("§3Quest Editor");
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem());
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openQuestEditor(p, quest);
				return false;
			}
		});
		
		int index = 9;
		for (final World world: Bukkit.getWorlds()) {
			menu.addItem(index, new ItemBuilder(Material.GRASS).display("&r" + world.getName() + ": " + (quest.isWorldEnabled(world.getName()) ? "&2&l\u2714": "&4&l\u2718")).get());
			menu.addMenuClickHandler(index, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					QuestChange changes = new QuestChange(quest);
					changes.toggleWorld(world.getName());
					if(changes.sendEvent())
						changes.apply();
					openWorldEditor(p, quest);
					return false;
				}
			});
			index++;
		}
		
		menu.open(p);
	}

	public static void openWorldEditor(Player p, final Category category) {
		final ChestMenu menu = new ChestMenu("§3Quest Editor");
		
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem());
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openCategoryEditor(p, category);
				return false;
			}
		});
		
		int index = 9;
		for (final World world: Bukkit.getWorlds()) {
			menu.addItem(index, new ItemBuilder(Material.GRASS).display("&r" + world.getName() + ": " + (category.isWorldEnabled(world.getName()) ? "&2&l\u2714": "&4&l\u2718")).get());
			menu.addMenuClickHandler(index, new MenuClickHandler() {
				
				@Override
				public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
					CategoryChange changes = new CategoryChange(category);
					changes.toggleWorld(world.getName());
					if(changes.sendEvent())
						changes.apply();
					
					openWorldEditor(p, category);
					return false;
				}
			});
			index++;
		}
		
		menu.open(p);
	}

	public static void openQuestMissionEditor(Player p, final Mission mission) {
		final ChestMenu menu = new ChestMenu("§3Quest Editor");
		MissionChange changes = new MissionChange(mission);
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem());
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openQuestEditor(p, mission.getQuest());
				return false;
			}
		});
		
		// Mission types now handle their own menu data!
		mission.getType().buildMenu(changes, menu);
		
		ItemStack missionSelector = new ItemBuilder(mission.getType().getSelectorItem())
				.display("&7" + mission.getType().toString())
				.lore(
						"",
						"&e> Click to change the Mission Type").get();
		
		menu.addItem(9, missionSelector);
		menu.addMenuClickHandler(9, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				openMissionSelector(p, changes.getSource());
				return false;
			}
		});
		
		menu.open(p);
	}

	public static void openMissionSelector(Player p, Mission mission) {
		final ChestMenu menu = new ChestMenu(Text.colorize("&3Mission Selector: " + mission.getQuest().getName()));
		MissionChange changes = new MissionChange(mission);
		menu.addMenuOpeningHandler(new MenuOpeningHandler() {
			
			@Override
			public void onOpen(Player p) {
				QuestWorld.getSounds().EditorClick().playTo(p);
			}
		});
		
		menu.addItem(0, new ItemBuilder(Material.MAP).display(QuestWorld.getInstance().getBookLocal("button.back.general")).get());
		menu.addMenuClickHandler(0, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
				openQuestMissionEditor(arg0, mission);
				return false;
			}
		});
		
		PagedMapping view = new PagedMapping(45, 9);
		int i = 0;
		for(MissionType type : QuestWorld.getInstance().getMissionTypes().values()) {
			String name = Text.niceName(type.getName());
			view.addItem(i, new ItemBuilder(type.getSelectorItem()).display("&f" + name).get());
			view.addButton(i, MissionButton.simpleHandler(changes, event -> changes.setType(type) ));
			++i;
		}
		view.setBackButton(Buttons.simpleHandler(event -> openQuestMissionEditor(p, mission)));
		view.build(menu, p);
		
		menu.open(p);
	}
}
