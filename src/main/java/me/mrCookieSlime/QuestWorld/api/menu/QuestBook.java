package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionSet;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
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
import me.mrCookieSlime.QuestWorld.container.PagedMapping;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.party.Party;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder.Proto;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QuestBook {
	
	public static void openMainMenu(Player p) {
		QuestWorld.getSounds().QUEST_CLICK.playTo(p);
		PlayerManager manager = PlayerManager.of(p);
		manager.update(false);
		manager.setLastEntry(null);
		
		Menu menu = new Menu(1, QuestWorld.translate(Translation.gui_title));
	
		PagedMapping view = new PagedMapping(45, 9);
		view.addFrameButton(4, partyMenuItem(p), Buttons.partyMenu(), true);

		for(ICategory category : QuestWorld.getFacade().getCategories()) {
			if (!category.isHidden()) {
				if (category.isWorldEnabled(p.getWorld().getName())) {
					if ((category.getParent() != null && !manager.hasFinished(category.getParent())) || !PlayerTools.checkPermission(p, category.getPermission())) {
						view.addButton(category.getID(),
								new ItemBuilder(Material.BARRIER).wrapText(
										category.getName(),
										"",
										QuestWorld.translate(Translation.quests_locked)).get(),
								null, false);
					}
					else {
						
						int questCount = manager.countQuests(category, null);
						int finishedCount = manager.getProgress(category);
						view.addButton(category.getID(),
								// TODO: wrapText
								new ItemBuilder(category.getItem()).lore(
										QuestWorld.translate(Translation.CATEGORY_DESC,
												String.valueOf(questCount),
												String.valueOf(finishedCount),
												String.valueOf(manager.countQuests(category, QuestStatus.AVAILABLE)),
												String.valueOf(manager.countQuests(category, QuestStatus.ON_COOLDOWN)),
												String.valueOf(manager.countQuests(category, QuestStatus.REWARD_CLAIMABLE)),
												Text.progressBar(finishedCount, questCount, null)
										).split("\n")).get(),
								event -> {
									Player p2 = (Player) event.getWhoClicked();
									PlayerManager.of(p2).putPage(0);
									openCategory(p2, category, true);
								}, true
						);
					}
				}
				else {
					view.addButton(category.getID(),
							new ItemBuilder(Material.BARRIER).wrapText(
									category.getName(),
									"",
									QuestWorld.translate(Translation.quests_locked_in_world)).get(),
							null, false);
				}
			}
		}
		view.build(menu, p);
		menu.openFor(p);
	}
	
	public static void openLastMenu(Player p) {
		IStateful last = PlayerManager.of(p).getLastEntry();
		
		if(last instanceof IQuest)
			QuestBook.openQuest(p, (IQuest)last, true, true);
		
		else if (last instanceof ICategory)
			QuestBook.openCategory(p, (ICategory)last, true);
		
		else
			QuestBook.openMainMenu(p);
	}
	
	private static ItemStack partyMenuItem(Player p) {
		String progress = PlayerManager.of(p).progressString();
		if (QuestWorld.getPlugin().getConfig().getBoolean("party.enabled")) {
			return new ItemBuilder(SkullType.PLAYER).wrapText(
					QuestWorld.translate(Translation.gui_party),
					progress,
					"",
					QuestWorld.translate(Translation.button_open)).get();
		}
		
		return new ItemBuilder(Material.ENCHANTED_BOOK).wrapText(
				"&eQuest Book",
				"",
				progress).get();
	}

	public static void openPartyMembers(final Player p) {
		QuestWorld.getSounds().PARTY_CLICK.playTo(p);
		
		Menu menu = new Menu(1, QuestWorld.translate(Translation.gui_party));

		ItemBuilder skull = new ItemBuilder(SkullType.PLAYER);
		menu.put(4,
				skull.wrapText(
						QuestWorld.translate(Translation.gui_party),
						"",
						QuestWorld.translate(Translation.button_back_party)).get(),
				event -> {
					openPartyMenu((Player) event.getWhoClicked());
				}
		);

		final Party party = PlayerManager.of(p).getParty();
		if (party != null) {
			for (int i = 0; i < party.getSize(); i++) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(party.getPlayers().get(i));
				if (!party.isLeader(p)) {
					menu.put(i + 9,
							skull.skull(player.getName()).wrapText(
									"&e" + player.getName(),
									"",
									(party.isLeader(player) ? "&4Party Leader": "&eParty Member")).get(),
							null
					);
				}
				else {
					menu.put(i + 9,
							skull.skull(player.getName()).wrapText(
									"&e" + player.getName(),
									"",
									(party.isLeader(player) ? "&5&lParty Leader": "&e&lParty Member"),
									"",
									(party.isLeader(player) ? "": "&7&oClick here to kick this Member")).get(),
							event -> {
								if (!party.isLeader(player)) {
									party.kickPlayer(player.getName());
									openPartyMembers((Player) event.getWhoClicked());
								}
							}
					);
				}
			}
		}
		
		menu.openFor(p);
	}

	public static void openPartyMenu(final Player p) {
		QuestWorld.getSounds().PARTY_CLICK.playTo(p);
		
		Menu menu = new Menu(2, QuestWorld.translate(Translation.gui_party));
		
		menu.put(4, new ItemBuilder(Material.MAP).wrapText(
				QuestWorld.translate(Translation.gui_title),
				"",
				QuestWorld.translate(Translation.button_back_quests)).get(),
				event -> {
					openMainMenu((Player) event.getWhoClicked());
				}
		);
		
		final Party party = PlayerManager.of(p).getParty();
		
		ItemBuilder wool = new ItemBuilder(Material.WOOL);
		
		if (party == null) {
			menu.put(9,
					wool.color(DyeColor.GREEN).wrapText(
							"&a&lCreate a new Party",
							"",
							"&rCreates a brand new Party for you", "&rto invite Friends and share your Progress").getNew(),
					event -> {
						Player p2 = (Player) event.getWhoClicked();
						Party.create(p2);
						openPartyMenu(p2);
					}
			);
		}
		else {
			if (party.isLeader(p)) {
				menu.put(9,
						wool.color(DyeColor.GREEN).wrapText(
								"&a&lInvite a Player",
								"",
								"&rInvites a Player to your Party Max. Party Members: &e"
								+QuestWorld.getPlugin().getConfig().getInt("party.max-members")).getNew(),
						event -> {
							Player p2 = (Player) event.getWhoClicked();
							if (party.getSize() >= QuestWorld.getPlugin().getConfig().getInt("party.max-members"))
								PlayerTools.sendTranslation(p2, true, Translation.PARTY_ERROR_FULL);
							else {
								PlayerTools.promptInput(p2, new SinglePrompt(
										PlayerTools.makeTranslation(true, Translation.PARTY_PLAYER_PICK),
										(c,s) -> {
											String name = Text.decolor(s).replace("@", "");

											Player player = PlayerTools.getPlayer(name);
											if (player != null) {
												if (PlayerManager.of(player).getParty() == null) {
													PlayerTools.sendTranslation(p2, true, Translation.PARTY_PLAYER_ADD, name);
													try {
														party.invitePlayer(player);
													} catch (Exception e1) {
														e1.printStackTrace();
													}
												}
												else PlayerTools.sendTranslation(p2, true, Translation.PARTY_ERROR_MEMBER, name);
											}
											else {
												PlayerTools.sendTranslation(p2, true, Translation.PARTY_ERROR_ABSENT, name);
											}
											return true;
										}
								));

								PlayerTools.closeInventoryWithEvent(p2);
							}
						}
				);
				
				menu.put(17,
						wool.color(DyeColor.RED).wrapText(
								"&4&lDelete your Party",
								"",
								"&rDeletes this Party",
								"&rBe careful with this Option!").getNew(),
						event -> {
							party.abandon();
							openPartyMenu((Player) event.getWhoClicked());
						}
				);
			}
			else {
				menu.put(17,
						wool.color(DyeColor.RED).wrapText(
								"&4&lLeave your Party",
								"",
								"&rLeaves this Party",
								"&rBe careful with this Option!").getNew(), 
						event -> {
							Player p2 = (Player) event.getWhoClicked();
							party.kickPlayer(p2.getName());
							openPartyMenu(p2);
						}
				);
			}
			
			menu.put(13,
					new ItemBuilder(SkullType.PLAYER).wrapText(
							"&eMember List",
							"",
							"&rShows you all Members of this Party").get(),
					event -> {
						openPartyMembers((Player) event.getWhoClicked());
					}
			);
		}
		
		menu.openFor(p);
	}

	public static void openCategory(Player p, ICategory category, final boolean back) {
		QuestWorld.getSounds().QUEST_CLICK.playTo(p);
		PlayerManager manager = PlayerManager.of(p);
		manager.update(false);
		manager.setLastEntry(category);
		
		Menu menu = new Menu(1, QuestWorld.translate(Translation.gui_title));
		ItemBuilder glassPane = new ItemBuilder(Material.STAINED_GLASS_PANE).color(DyeColor.RED);
		PagedMapping view = new PagedMapping(45, 9);
		
		if(back)
			view.setBackButton(event -> {
				openMainMenu((Player) event.getWhoClicked());
			});
		
		view.addFrameButton(4, partyMenuItem(p), Buttons.partyMenu(), true);
		
		for (final IQuest quest: category.getQuests()) {
			// TODO: wrapText
			glassPane.display(quest.getName());
			if (manager.getStatus(quest).equals(QuestStatus.LOCKED) || !quest.getWorldEnabled(p.getWorld().getName())) {
				view.addButton(quest.getID(), glassPane.lore("", QuestWorld.translate(Translation.quests_locked)).getNew(),
						null, false);
			}
			else if (manager.getStatus(quest).equals(QuestStatus.LOCKED_NO_PARTY)) {
				view.addButton(quest.getID(), glassPane.lore("", "&4You need to leave your current Party").getNew(),
						null, false);
			}
			else if (manager.getStatus(quest).equals(QuestStatus.LOCKED_PARTY_SIZE)) {
				view.addButton(quest.getID(), glassPane.lore("", "&4You can only do this Quest in a Party", "&4with at least &c" + quest.getPartySize() + " &4Members").getNew(),
						null, false);
			}
			else {
				// TODO Combine these translations and use placeholders
				List<String> lore = new ArrayList<String>();
				lore.add("");
				lore.add(manager.progressString(quest));
				lore.add("");
				lore.add(Text.colorize("&7") + manager.getProgress(quest) + "/" + quest.getMissions().size() + QuestWorld.translate(Translation.quests_tasks_completed));
				if (manager.getStatus(quest).equals(QuestStatus.REWARD_CLAIMABLE)) {
					lore.add("");
					lore.add(QuestWorld.translate(Translation.quests_state_reward_claimable));
				}
				else if (manager.getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
					lore.add("");
					lore.add(QuestWorld.translate(Translation.quests_state_cooldown));
				}
				else if (manager.hasFinished(quest)) {
					lore.add("");
					lore.add(QuestWorld.translate(Translation.quests_state_completed));
				}
				for(int i = 0; i < lore.size(); ++i)
					lore.set(i, Text.colorize(lore.get(i)));
				
				view.addButton(quest.getID(),
						new ItemBuilder(quest.getItem()).lore(lore)
						.get(),
						event -> {
							openQuest((Player) event.getWhoClicked(), quest, back, true);
						}, true
				);
			}
		}
		view.build(menu, p);
		menu.openFor(p);
	}
	
	public static void openQuest(final Player p, final IQuest quest, final boolean categoryBack, final boolean back) {
		QuestWorld.getSounds().QUEST_CLICK.playTo(p);
		PlayerManager manager = PlayerManager.of(p);
		manager.update(false);
		manager.setLastEntry(quest);
		
		Menu menu = new Menu(3, QuestWorld.translate(Translation.gui_title));
		
		if (back) {
			menu.put(0, ItemBuilder.Proto.MAP_BACK.getItem(), event -> {
				openCategory((Player) event.getWhoClicked(), quest.getCategory(), categoryBack);
			});
		}
		
		// Detect all
		menu.put(1,
				new ItemBuilder(Material.CHEST).display("&7Check all Tasks").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					PlayerManager manager2 = PlayerManager.of(p2);
					for(IMission mission : quest.getMissions()) {
						if (!manager2.hasUnlockedTask(mission)) continue;
						if (manager2.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.getWorldEnabled(p2.getWorld().getName())) {
							if (manager2.hasCompletedTask(mission)) continue;
							
							if(mission.getType() instanceof Manual) {
								MissionSet.Result entry = new MissionSet.Result(mission, manager2.getProgress(mission));
								((Manual) mission.getType()).onManual(p2, entry);
								manager2.setProgress(mission, entry.getProgress());
								openQuest(p2, quest, categoryBack, back);
							}
						}
					}
				}
		);
		
		if (quest.getCooldown() >= 0) {
			String cooldown = quest.getFormattedCooldown();
			if (manager.getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
				long remaining = (manager.getCooldownEnd(quest) - System.currentTimeMillis() + 59999) / 60 / 1000;
				cooldown = Text.timeFromNum(remaining) + " remaining";
			}
			menu.put(8, new ItemBuilder(Material.WATCH).wrapText(
					QuestWorld.translate(Translation.quests_display_cooldown),
					"",
					"&b" + cooldown).get(),
					null
			);
		}
		
		int rewardIndex = 2;
		if (quest.getMoney() > 0 && QuestWorld.getEconomy() != null) {
			menu.put(rewardIndex, new ItemBuilder(Material.GOLD_INGOT).wrapText(
					QuestWorld.translate(Translation.quests_display_monetary),
					"",
					"&6$" + quest.getMoney()).get(),
					null
			);
			rewardIndex++;
		}
		
		if (quest.getXP() > 0) {
			menu.put(rewardIndex, new ItemBuilder(Material.EXP_BOTTLE).wrapText(
					QuestWorld.translate(Translation.quests_display_exp),
					"",
					"&a" + quest.getXP() + " Level").get(),
					null
			);
			rewardIndex++;
		}
		
		ItemBuilder glassPane = new ItemBuilder(Material.STAINED_GLASS_PANE);
		
		int index = 9;
		for (final IMission mission: quest.getMissions()) {
			ItemStack item = glassPane.get();
			if (manager.hasUnlockedTask(mission)) {
				ItemBuilder entryItem = new ItemBuilder(mission.getDisplayItem());
				int current = manager.getProgress(mission);
				int total = mission.getAmount();
				String progress = Text.progressBar(current, total, mission.getType().progressString(current, total));
				
				if(mission.getType() instanceof Manual) {
					String label = ((Manual) mission.getType()).getLabel();
					entryItem.wrapText(
							mission.getText(),
							"",
							progress,
							"",
							"&r> Click for Manual " + label);
				}
				else
					entryItem.wrapText(
							mission.getText(),
							"",
							progress);

				item = entryItem.get();
			}
			else
				item = glassPane.color(DyeColor.RED).wrapText(
						"&7&kSOMEWEIRDMISSION",
						"",
						QuestWorld.translate(Translation.task_locked)).getNew();
			
			menu.put(index, item, event -> {
				Player p2 = (Player) event.getWhoClicked();
				PlayerManager manager2 = PlayerManager.of(p2);
				
				if (!manager2.hasUnlockedTask(mission)) return;
				if (manager2.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.getWorldEnabled(p2.getWorld().getName())) {
					if (manager2.hasCompletedTask(mission)) return;
					
					if(mission.getType() instanceof Manual) {
						MissionSet.Result entry = new MissionSet.Result(mission, manager2.getProgress(mission));
						((Manual) mission.getType()).onManual(p2, entry);
						manager2.setProgress(mission, entry.getProgress());
						openQuest(p2, quest, categoryBack, back);
					}
				}
			});
			index++;
		}
		
		for (int i = 0; i < 9; i++) {
			if (manager.getStatus(quest).equals(QuestStatus.REWARD_CLAIMABLE)) {
				menu.put(i + 18,
						glassPane.color(DyeColor.PURPLE).wrapText(
								QuestWorld.translate(Translation.quests_state_reward_claim)).get(),
						event -> {
							Player p2 = (Player) event.getWhoClicked();
							// TODO manually handout and complete... reconsider
							quest.handoutReward(p2);
							PlayerManager.of(p2).completeQuest(quest);
							// TODO QuestWorld.getSounds().muteNext();
							openQuest(p2, quest, categoryBack, back);
						}
				);
			}
			else if (manager.getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
				menu.put(i + 18,
						glassPane.color(DyeColor.YELLOW).wrapText(
								QuestWorld.translate(Translation.quests_state_cooldown)).get(),
						null
				);
			}
			else {
				menu.put(i + 18,
						glassPane.color(DyeColor.GRAY).wrapText(
								QuestWorld.translate(Translation.quests_display_rewards)).get(),
						null);
			}
		}
		
		int slot = 27;
		for (ItemStack reward: quest.getRewards()) {
			menu.put(slot, reward, null);
			slot++;
		}
		
		menu.openFor(p);
	}

	
	/*
	 * 
	 * 			Quest Editor
	 * 
	 */
	public static void openEditor(Player p) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		final Menu menu = new Menu(6, "&3Quest Editor");
		
		ItemBuilder defaultItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
				.color(DyeColor.RED).display("&7&o> New Category");

		PagedMapping view = new PagedMapping(45);
		view.reserve(1);
		
		for(int i = 0; i < view.getCapacity(); ++i) {
			ICategory category = QuestWorld.getFacade().getCategory(i);
			if(category != null) {
				String[] lore = {
						"",
						"&c&oLeft Click to open",
						"&c&oShift + Left Click to edit",
						"&c&oRight Click to delete"
				};
				int quests = category.getQuests().size();
				if(quests > 0) {
					int j = 0;
					List<String> lines = new ArrayList<>();
					for(IQuest q : category.getQuests()) {
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
				
				view.addButton(i,
						new ItemBuilder(category.getItem()).lore(lore).get(),
						Buttons.onCategory(category), true);

				view.reserve(1);
			}
			else
				view.addButton(i, defaultItem.get(), Buttons.newCategory(i), true);
		}

		view.build(menu, p);
		menu.openFor(p);
	}

	public static void openCategoryQuestEditor(Player p, final ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		final Menu menu = new Menu(6, "&3Quest Editor");

		ItemBuilder defaultItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
				.color(DyeColor.RED).display("&7&o> New Quest");
		
		PagedMapping view = new PagedMapping(45);
		view.reserve(1);
		view.setBackButton(event -> {
			openEditor((Player) event.getWhoClicked());
		});

		for (int i = 0; i < view.getCapacity(); ++i) {
			IQuest quest = category.getQuest(i);
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
					for(IMission m : quest.getMissions()) {
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
				
				view.addButton(i,
						new ItemBuilder(quest.getItem()).lore(lore).get(),
						Buttons.onQuest(quest), true);
				
				view.reserve(1);
			}
			else
				view.addButton(i, defaultItem.getNew(), Buttons.newQuest(category, i), true);
		}
		view.build(menu, p);
		menu.openFor(p);
	}

	public static void openCategoryEditor(Player p, final ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		final Menu menu = new Menu(2, "&3Quest Editor");
		ICategoryState changes = category.getState();
		
		menu.put(0,  Proto.MAP_BACK.getItem(), event -> {
			openEditor((Player) event.getWhoClicked());
		});
		
		menu.put(9,
				new ItemBuilder(category.getItem()).wrapText(
						null,
						"",
						"&e> Click to set the Item to the one in your hand").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					ItemStack hand = PlayerTools.getActiveHandItem(p2);
					if (hand != null) {
						changes.setItem(hand);
						if(changes.apply()) {
							
						}
						openCategoryEditor(p2, category);
				}
		});
		
		menu.put(10,
				new ItemBuilder(Material.NAME_TAG).wrapText(
						category.getName(),
						"",
						"&e> Click to change the Name").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					PlayerTools.promptInput(p2, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.CATEGORY_NAME_EDIT, category.getName()),
							(c,s) -> {
								String oldName = category.getName();
								changes.setName(s);
								if(changes.apply())
									PlayerTools.sendTranslation(p2, true, Translation.CATEGORY_NAME_SET, s, oldName);
								
								QuestBook.openCategoryEditor(p2, category);
								return true;
							}
					));

					PlayerTools.closeInventoryWithEvent(p2);
				}
		);
		
		menu.put(11,
				new ItemBuilder(Material.BOOK_AND_QUILL).wrapText(
						"&7Quest Requirement:",
						"",
						(category.getParent() != null ? "&r" + category.getParent().getName(): "&7&oNone"),
						"",
						"&rLeft Click: &eChange Quest Requirement",
						"&rRight Click: &eRemove Quest Requirement").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					if (event.isRightClick()) {
						changes.setParent(null);
						if(changes.apply()) {
							
						}
						openCategoryEditor(p2, category);
					}
					else {
						PlayerManager.of(p2).putPage(0);
						QBDialogue.openQuestRequirementChooser(p2, category);
					}
				}
		);
		
		menu.put(12,
				new ItemBuilder(Material.NAME_TAG).wrapText(
						"&r" + (category.getPermission().equals("") ? "None": category.getPermission()),
						"",
						"&e> Click to change the rquired Permission Node").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					PlayerTools.promptInput(p2, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.CATEGORY_PERM_EDIT, category.getName(), category.getPermission()),
							(c,s) -> {
								String permission = s.equalsIgnoreCase("none") ? "": s;
								String oldPerm = category.getPermission();
								changes.setPermission(permission);
								if(changes.apply())
									PlayerTools.sendTranslation(p2, true, Translation.CATEGORY_PERM_SET, category.getName(), s, oldPerm);
								
								QuestBook.openCategoryEditor(p2, category);
								return true;
							}
					));
					
					PlayerTools.closeInventoryWithEvent(p2);
				}
		);
		
		menu.put(13,
				new ItemBuilder(Material.GOLDEN_CARROT).wrapText(
						"&rShow in Quest Book: " + (!category.isHidden() ? "&2&l\u2714": "&4&l\u2718"),
						"",
						"&e> Click to change whether this Category will appear in the Quest Book").get(),
				event -> {
					changes.setHidden(!category.isHidden());
					if(changes.apply()) {
					}
					openCategoryEditor((Player) event.getWhoClicked(), category);
				}
		);
		
		menu.put(14,
				new ItemBuilder(Material.GRASS).wrapText(
						"&7World Blacklist",
						"",
						"&e> Click to configure in which Worlds this Category is enabled").get(),
				event -> {
					openWorldEditor((Player) event.getWhoClicked(), category);
				}
		);
		
		menu.put(17,
				ItemBuilder.Proto.RED_WOOL.get().wrapText(
						"&4Delete Database",
						"",
						"&rThis is going to delete the Database of all Quests inside this Category"
						+" and will clear all Player's Progress associated with those Quests.").get(),
				event -> {
					for (IQuest quest: category.getQuests()) {
						PlayerManager.clearAllQuestData(quest);
					}
					QuestWorld.getSounds().DESTRUCTIVE_CLICK.playTo((Player) event.getWhoClicked());
				}
		);
		
		menu.openFor(p);
	}

	public static void openQuestEditor(Player p, final IQuest quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		final Menu menu = new Menu(6, "&3Quest Editor");
		IQuestState changes = quest.getState();
		
		menu.put(0, ItemBuilder.Proto.MAP_BACK.getItem(), event -> {
			openCategoryQuestEditor((Player) event.getWhoClicked(), quest.getCategory());
		});
		
		menu.put(9,
				new ItemBuilder(quest.getItem()).wrapText(
						null,
						"",
						"&e> Click to set the Item to the one in your hand").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					ItemStack mainItem = p2.getInventory().getItemInMainHand();
					if (mainItem != null) {
						changes.setItem(mainItem);
						changes.apply();
						
						openQuestEditor(p2, quest);
					}
				}
		);
		
		menu.put(10,
				new ItemBuilder(Material.NAME_TAG).wrapText(
						quest.getName(),
						"",
						"&e> Click to change the Name").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					PlayerTools.promptInput(p2, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.QUEST_NAME_EDIT, quest.getName()),
							(c,s) -> {
								String oldName = quest.getName();
								changes.setName(Text.colorize(s));
								if(changes.apply()) 
									PlayerTools.sendTranslation(p2, true, Translation.QUEST_NAME_SET, s, oldName);

								QuestBook.openQuestEditor(p2, quest);
								return true;
							}
					));

					PlayerTools.closeInventoryWithEvent(p2);
				}
		);
		
		menu.put(11,
				new ItemBuilder(Material.CHEST).wrapText(
						"&rRewards &7(Item)",
						"",
						"&e> Click to change the Rewards to be the Items in your Hotbar").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					changes.setItemRewards(p2);
					changes.apply();
					
					openQuestEditor(p2, quest);
				}
		);
		
		menu.put(12,
				new ItemBuilder(Material.WATCH).wrapText(
						"&7Cooldown: &b" + quest.getFormattedCooldown(),
						"",
						"&rLeft Click: &e+1m",
						"&rRight Click: &e-1m",
						"&rShift + Left Click: &e+1h",
						"&rShift + Right Click: &e-1h").get(),
				event -> {
					long cooldown = quest.getCooldown();
					long delta = event.isShiftClick() ? 60: 1;
					if (event.isRightClick()) delta = -delta;

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
					changes.apply();
					
					openQuestEditor((Player) event.getWhoClicked(), quest);
				}
		);
		
		if (QuestWorld.getEconomy() != null) {
			menu.put(13,
					new ItemBuilder(Material.GOLD_INGOT).wrapText(
							"&7Monetary Reward: &6$" + quest.getMoney(),
							"",
							"&rLeft Click: &e+1",
							"&rRight Click: &e-1",
							"&rShift + Left Click: &e+100",
							"&rShift + Right Click: &e-100").get(),
					event -> {
						int money = quest.getMoney();
						if (event.isRightClick()) money = money - (event.isShiftClick() ? 100: 1);
						else money = money + (event.isShiftClick() ? 100: 1);
						if (money < 0) money = 0;
						changes.setMoney(money);
						changes.apply();
						openQuestEditor((Player) event.getWhoClicked(), quest);
					}
			);
		}
		
		menu.put(14,
				new ItemBuilder(Material.EXP_BOTTLE).wrapText(
						"&7XP Reward: &b" + quest.getXP() + " Level",
						"",
						"&rLeft Click: &e+1",
						"&rRight Click: &e-1",
						"&rShift + Left Click: &e+10",
						"&rShift + Right Click: &e-10").get(),
				event -> {
					int xp = quest.getXP();
					if (event.isRightClick()) xp = xp - (event.isShiftClick() ? 10: 1);
					else xp = xp + (event.isShiftClick() ? 10: 1);
					if (xp < 0) xp = 0;
					changes.setXP(xp);
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				}
		);
		
		menu.put(15,
				new ItemBuilder(Material.BOOK_AND_QUILL).wrapText(
						"&7Quest Requirement:",
						"",
						(quest.getParent() != null ? "&r" + quest.getParent().getName(): "&7&oNone"),
						"",
						"&rLeft Click: &eChange Quest Requirement",
						"&rRight Click: &eRemove Quest Requirement").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					if (event.isRightClick()) {
						changes.setParent(null);
						changes.apply();
						openQuestEditor(p2, quest);
					}
					else {
						PlayerManager.of(p2).putPage(0);
						QBDialogue.openQuestRequirementChooser(p2, quest);
					}
				}
		);
		
		menu.put(16,
				new ItemBuilder(Material.COMMAND).wrapText(
						"&7Commands executed upon Completion",
						"",
						"&rLeft Click: &eOpen Command Editor").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					PlayerTools.closeInventoryWithEvent(p2);
					QBDialogue.openCommandEditor(p2, quest);
				}
		);
		
		menu.put(17,
				new ItemBuilder(Material.NAME_TAG).wrapText(
						"&r" + (quest.getPermission().equals("") ? "None": quest.getPermission()),
						"",
						"&e> Click to change the required Permission Node").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					PlayerTools.promptInput(p2, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.QUEST_PERM_EDIT, quest.getName(), quest.getPermission()),
							(c,s) -> {
								String permission = s.equalsIgnoreCase("none") ? "": s;
								String oldPerm = quest.getPermission();
								changes.setPermission(permission);
								if(changes.apply())
									PlayerTools.sendTranslation(p2, true, Translation.QUEST_PERM_SET, quest.getName(), s, oldPerm);

								QuestBook.openQuestEditor(p2, quest);
								return true;
							}
					));

					PlayerTools.closeInventoryWithEvent(p2);
				}
		);
		
		menu.put(18,
				new ItemBuilder(Material.FIREWORK).wrapText(
						"&rParty Support: " + (quest.supportsParties() ? "&2&l\u2714": "&4&l\u2718"),
						"",
						"&e> Click to change whether this Quest can be done in Parties or not").get(),
				event -> {
					changes.setPartySupport(!quest.supportsParties());
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				}
		);
		
		menu.put(19,
				new ItemBuilder(Material.COMMAND).wrapText(
						"&rOrdered Completion Mode: " + (quest.getOrdered() ? "&2&l\u2714": "&4&l\u2718"),
						"",
						"&e> Click to change whether this Quest's Tasks have to be done in the Order they are arranged").get(),
				event -> {
					changes.setOrdered(!quest.getOrdered());
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				}
		);
		
		menu.put(20,
				new ItemBuilder(Material.CHEST).wrapText(
						"&rAuto-Claim Rewards: " + (quest.getAutoClaimed() ? "&2&l\u2714": "&4&l\u2718"),
						"",
						"&e> Click to change whether this Quest's Rewards will be"
						+" automatically given or have to be claimed manually").get(),
				event -> {
					changes.setAutoClaim(!changes.getAutoClaimed());
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				}
		);
		
		menu.put(21,
				new ItemBuilder(Material.GRASS).wrapText(
						"&7World Blacklist",
						"",
						"&e> Click to configure in which Worlds this Quest is able to be completed").get(),
				event -> {
					openWorldEditor((Player) event.getWhoClicked(), quest);
				}
		);
		
		String wtfString = "&rMinimal Party Size: " + (quest.getPartySize() < 1 ? "&4Players aren't allowed be in a Party": (quest.getPartySize() == 1 ? ("&ePlayers can but don't have to be in a Party") : ("&aPlayers need to be in a Party of " + quest.getPartySize() + " or more")));
		menu.put(22,
				new ItemBuilder(Material.FIREWORK).wrapText(
						wtfString,
						"",
						"&eChange the min. Amount of Players in a Party needed to start this Quest",
						"",
						"&r1 = &7Players can but don't have to be in a Party",
						"&r0 = &7Players aren't allowed to be in a Party",
						"",
						"&rLeft Click: &e+1",
						"&rRight Click: &e-1").get(),
				event -> {
					int size = quest.getPartySize();
					if (event.isRightClick()) size--;
					else size++;
					if (size < 0) size = 0;
					changes.setPartySize(size);
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				}
		);
		
		menu.put(26,
				ItemBuilder.Proto.RED_WOOL.get().wrapText(
						"&4Delete Database",
						"",
						"&rThis is going to delete this Quest's Database and will"
						+" clear all Player's Progress associated with this Quest.").get(),
				event -> {
					PlayerManager.clearAllQuestData(quest);
					QuestWorld.getSounds().DESTRUCTIVE_CLICK.playTo((Player) event.getWhoClicked());
				}
		);
		
		int index = 36;
		for (ItemStack reward: quest.getRewards()) {
			menu.put(index, reward, null);
			index++;
		}
		
		for (int i = 0; i < 9; i++) {
			final IMission mission = quest.getMission(i);
			if (mission == null) {
				menu.put(45 + i,
						new ItemBuilder(Material.PAPER).display("&7&o> New Task").get(),
						event -> {
							changes.addMission(event.getSlot() + 9);

							changes.apply();
							openQuestEditor((Player) event.getWhoClicked(), quest);
						});
			}
			else {
				menu.put(45 + i,
						new ItemBuilder(Material.BOOK).wrapText(
								mission.getText(),
								"",
								"&c&oLeft Click to edit",
								"&c&oRight Click to delete").get(),
						event -> {
							Player p2 = (Player) event.getWhoClicked();
							if (event.isRightClick()) QBDialogue.openDeletionConfirmation(p2, mission);
							else openQuestMissionEditor(p2, mission);
						}
				);
			}
		}
		
		menu.openFor(p);
	}

	public static void openWorldEditor(Player p, final IQuest quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		final Menu menu = new Menu(2, "&3Quest Editor");
		
		menu.put(0, ItemBuilder.Proto.MAP_BACK.getItem(), event -> {
			openQuestEditor((Player) event.getWhoClicked(), quest);
		});
		
		int index = 9;
		for (final World world: Bukkit.getWorlds()) {
			menu.put(index,
					new ItemBuilder(Material.GRASS)
					.display("&r" + world.getName() + ": " + (quest.getWorldEnabled(world.getName()) ? "&2&l\u2714": "&4&l\u2718")).get(),
					event -> {
						IQuestState changes = quest.getState();
						changes.toggleWorld(world.getName());
						if(changes.apply()) {
						}

						openWorldEditor((Player) event.getWhoClicked(), quest);
					}
			);
			index++;
		}
		
		menu.openFor(p);
	}

	public static void openWorldEditor(Player p, final ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		final Menu menu = new Menu(2, "&3Quest Editor");
		
		menu.put(0, ItemBuilder.Proto.MAP_BACK.getItem(), event -> {
			openCategoryEditor((Player) event.getWhoClicked(), category);
		});
		
		int index = 9;
		for (final World world: Bukkit.getWorlds()) {
			menu.put(index,
					new ItemBuilder(Material.GRASS)
					.display("&r" + world.getName() + ": " + (category.isWorldEnabled(world.getName()) ? "&2&l\u2714": "&4&l\u2718")).get(),
					event -> {
						ICategoryState changes = category.getState();
						changes.toggleWorld(world.getName());
						if(changes.apply()) {
						}
						
						openWorldEditor((Player) event.getWhoClicked(), category);
					}
			);
			index++;
		}
		
		menu.openFor(p);
	}

	public static void openQuestMissionEditor(Player p, final IMission mission) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);
		
		Menu menu = new Menu(2, "&3Quest Editor");

		menu.put(0, ItemBuilder.Proto.MAP_BACK.getItem(), e -> {
			openQuestEditor(p, mission.getQuest());
		});
		
		// Mission types now handle their own menu data!
		mission.getType().buildMenu(mission.getState(), menu);
		
		ItemStack missionSelector = new ItemBuilder(mission.getType().getSelectorItem()).wrapText(
				"&7" + mission.getType().toString(),
				"",
				"&e> Click to change the Mission Type").get();
		
		menu.put(9, missionSelector, e -> {
			openMissionSelector(p, mission);
		});
		
		menu.openFor(p);
	}

	public static void openMissionSelector(Player p, IMission mission) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		IMissionState changes = mission.getState();
		final Menu menu = new Menu(3, Text.colorize("&3Mission Selector: " + mission.getQuest().getName()));

		PagedMapping view = new PagedMapping(45, 9);
		view.setBackButton(event -> {
			openQuestMissionEditor((Player) event.getWhoClicked(), mission);
		});
		int i = 0;
		for(MissionType type : QuestWorld.getMissionTypes().values()) {
			String name = Text.niceName(type.getName());
			view.addButton(i,
					new ItemBuilder(type.getSelectorItem()).display("&f" + name).get(),
					event -> {
				changes.setType(type);
				MissionButton.apply(event, changes);
			}, false);
			++i;
		}
		view.setBackButton(event -> openQuestMissionEditor(p, mission));
		view.build(menu, p);
		
		menu.openFor(p);
	}
}
