package com.questworld.api.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.questworld.Constants;
import com.questworld.api.Manual;
import com.questworld.api.MissionType;
import com.questworld.api.QuestStatus;
import com.questworld.api.QuestWorld;
import com.questworld.api.SinglePrompt;
import com.questworld.api.Translation;
import com.questworld.api.contract.DataObject;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.ICategoryState;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.IParty;
import com.questworld.api.contract.IParty.LeaveReason;
import com.questworld.api.contract.IPlayerStatus;
import com.questworld.api.contract.IQuest;
import com.questworld.api.contract.IQuestState;
import com.questworld.util.ItemBuilder;
import com.questworld.util.ItemBuilder.Proto;
import com.questworld.util.PlayerTools;
import com.questworld.util.Text;

public class QuestBook {
	public static DataObject getLastViewed(Player p) {
		List<MetadataValue> metadata = p.getMetadata(Constants.MD_LAST_MENU);
		
		if(metadata.isEmpty()) {
			return null;
		}
		
		Object value = metadata.get(0).value();
		
		// Every ticket involving /reload JUST STOP DOING IT GUYS GOOD LORD THIS ISN'T RELOAD SAFE
		if(!(value instanceof DataObject)) {
			p.removeMetadata(Constants.MD_LAST_MENU, QuestWorld.getPlugin());
			return null;
		}
		
		return (DataObject) value;
	}

	public static boolean testCategory(Player p, ICategory category) {
		IQuest parent = category.getParent();
		IPlayerStatus playerStatus = QuestWorld.getPlayerStatus(p);

		return !category.isHidden() && category.isWorldEnabled(p.getWorld().getName())
				&& PlayerTools.checkPermission(p, category.getPermission())
				&& (parent == null || playerStatus.hasFinished(parent));
	}

	public static boolean testQuest(Player p, IQuest quest) {
		QuestStatus status = QuestWorld.getPlayerStatus(p).getStatus(quest);

		return quest.isEnabled() && (status == QuestStatus.AVAILABLE || status == QuestStatus.REWARD_CLAIMABLE
				|| status == QuestStatus.ON_COOLDOWN || status == QuestStatus.FINISHED);
	}
	
	public static void clearLastViewed(Player p) {
		setLastViewed(p, null, true, true);
	}

	public static void setLastViewed(Player p, DataObject object) {
		p.setMetadata(Constants.MD_LAST_MENU, new FixedMetadataValue(QuestWorld.getPlugin(), object));
	}

	private static void setLastViewed(Player p, DataObject object, boolean catBack) {
		setLastViewed(p, object);
		
		if(!catBack) {
			p.setMetadata(Constants.MD_NO_CAT_BACK, new FixedMetadataValue(QuestWorld.getPlugin(), catBack));
		}
		else {
			p.removeMetadata(Constants.MD_NO_CAT_BACK, QuestWorld.getPlugin());
		}
	}

	private static void setLastViewed(Player p, DataObject object, boolean catBack, boolean questBack) {
		setLastViewed(p, object, catBack);
		
		if(!questBack) {
			p.setMetadata(Constants.MD_NO_QUEST_BACK, new FixedMetadataValue(QuestWorld.getPlugin(), questBack));
		}
		else {
			p.removeMetadata(Constants.MD_NO_QUEST_BACK, QuestWorld.getPlugin());
		}
	}
	
	private static boolean getCategoryBack(Player p) {
		return !p.hasMetadata(Constants.MD_NO_CAT_BACK);
	}
	
	private static boolean getQuestBack(Player p) {
		return !p.hasMetadata(Constants.MD_NO_QUEST_BACK);
	}

	public static void openMainMenu(Player p) {
		QuestWorld.getSounds().QUEST_CLICK.playTo(p);
		IPlayerStatus playerStatus = QuestWorld.getPlayerStatus(p);
		playerStatus.update();

		Menu menu = new Menu(1, QuestWorld.translate(p, Translation.gui_title));

		PagedMapping view = new PagedMapping(45, 9);
		view.addFrameButton(4, partyMenuItem(p), Buttons.partyMenu(), true);

		for (ICategory category : QuestWorld.getFacade().getCategories()) {
			IQuest parent = category.getParent();

			if (!category.isHidden()) {
				if (!category.isWorldEnabled(p.getWorld().getName())) {
					view.addButton(category.getID(),
							new ItemBuilder(Material.BARRIER)
									.wrapText(category.getName(), "",
											QuestWorld.translate(p, Translation.LOCKED_WORLD, p.getWorld().getName()))
									.get(),
							null, false);
				}
				else if (!PlayerTools.checkPermission(p, category.getPermission())) {
					String parts[] = category.getPermission().split(" ", 2);
					view.addButton(category.getID(),
							new ItemBuilder(Material.BARRIER).wrapText(category.getName(), "",
									QuestWorld.translate(p, Translation.LOCKED_NO_PERM, parts[0], parts[parts.length - 1]))
									.get(),
							null, false);
				}
				else if (parent != null && !playerStatus.hasFinished(parent)) {
					view.addButton(category.getID(),
							new ItemBuilder(Material.BARRIER).wrapText(category.getName(), "",
									QuestWorld.translate(p, Translation.LOCKED_PARENT, category.getParent().getName()))
									.get(),
							null, false);
				}
				else {

					int questCount = playerStatus.countQuests(category, null);
					int finishedCount = playerStatus.getProgress(category);
					view.addButton(category.getID(),
							new ItemBuilder(category.getItem()).wrapText((category.getName() + "\n"
									+ QuestWorld.translate(p, Translation.CATEGORY_DESC, String.valueOf(questCount),
											String.valueOf(finishedCount),
											String.valueOf(playerStatus.countQuests(category, QuestStatus.AVAILABLE)),
											String.valueOf(playerStatus.countQuests(category, QuestStatus.ON_COOLDOWN)),
											String.valueOf(
													playerStatus.countQuests(category, QuestStatus.REWARD_CLAIMABLE)),
											Text.progressBar(finishedCount, questCount, null))).split("\n"))
									.get(),
							event -> {
								Player p2 = (Player) event.getWhoClicked();
								PagedMapping.putPage(p2, 0);
								openCategory(p2, category, true);
							}, true);
				}
			}
		}
		view.build(menu, p);
		menu.openFor(p);
	}

	public static void openLastMenu(Player p) {
		DataObject last = getLastViewed(p);
		
		boolean catBack = getCategoryBack(p);
		boolean questBack = getQuestBack(p);

		if (last instanceof IQuest)
			QuestBook.openQuest(p, (IQuest) last, catBack, questBack);

		else if (last instanceof ICategory)
			QuestBook.openCategory(p, (ICategory) last, catBack);

		else
			QuestBook.openMainMenu(p);
	}

	private static ItemStack partyMenuItem(Player p) {
		String progress = QuestWorld.getPlayerStatus(p).progressString();
		if (QuestWorld.getPlugin().getConfig().getBoolean("party.enabled")) {
			return new ItemBuilder(Material.PLAYER_HEAD).wrapText(QuestWorld.translate(p, Translation.gui_party), progress, "",
					QuestWorld.translate(p, Translation.button_open)).get();
		}

		return new ItemBuilder(Material.ENCHANTED_BOOK)
				.wrapText(QuestWorld.translate(p, Translation.gui_title), "", progress).get();
	}

	public static void openPartyMembers(final Player p) {
		QuestWorld.getSounds().PARTY_CLICK.playTo(p);

		Menu menu = new Menu(1, QuestWorld.translate(p, Translation.gui_party));

		ItemBuilder skull = new ItemBuilder(Material.PLAYER_HEAD);
		menu.put(4, skull.wrapText(QuestWorld.translate(p, Translation.gui_party), "",
				QuestWorld.translate(p, Translation.button_back_party)).get(), event -> {
					openPartyMenu((Player) event.getWhoClicked());
				});

		final IParty party = QuestWorld.getParty(p);
		if (party != null) {
			int i = 0;
			for (OfflinePlayer member : party.getFullGroup()) {
				if (!party.isLeader(p)) {
					menu.put(i + 9,
							skull.skull(member)
									.wrapText("&e" + member.getName(), "",
											(party.isLeader(member) ? "&4Party Leader" : "&eParty Member"))
									.get(),
							null);
				}
				else {
					menu.put(i + 9,
							skull.skull(member)
									.wrapText("&e" + member.getName(), "",
											(party.isLeader(member) ? "&5&lParty Leader" : "&e&lParty Member"), "",
											(party.isLeader(member) ? "" : "&7&oClick here to kick this Member"))
									.get(),
							event -> {
								if (!party.isLeader(member)) {
									party.playerLeave(member, LeaveReason.KICKED);
									openPartyMembers((Player) event.getWhoClicked());
								}
							});
				}
				++i;
			}
		}

		menu.openFor(p);
	}

	public static void openPartyMenu(final Player p) {
		QuestWorld.getSounds().PARTY_CLICK.playTo(p);

		Menu menu = new Menu(2, QuestWorld.translate(p, Translation.gui_party));

		menu.put(4, new ItemBuilder(Material.MAP).flagAll().wrapText(QuestWorld.translate(p, Translation.gui_title), "",
				QuestWorld.translate(p, Translation.button_back_quests)).get(), event -> {

					DataObject s = QuestBook.getLastViewed(p);
					if (s instanceof IQuest) {
						openQuest((Player) event.getWhoClicked(), (IQuest) s, true, true);
					}
					else if (s instanceof ICategory) {
						openCategory((Player) event.getWhoClicked(), (ICategory) s, true);
					}
					else
						openMainMenu((Player) event.getWhoClicked());
				});

		final IParty party = QuestWorld.getParty(p);

		ItemBuilder wool = new ItemBuilder(Material.GREEN_WOOL);

		if (party == null) {
			menu.put(9,
					wool.wrapText("&a&lCreate a new Party", "",
							"&rCreates a brand new Party for you",
							"&rto invite Friends and share your Progress")
							.get(),
					event -> {
						Player p2 = (Player) event.getWhoClicked();
						QuestWorld.createParty(p2);
						openPartyMenu(p2);
					});
		}
		else {
			if (party.isLeader(p)) {
				menu.put(9, wool
						.wrapText("&a&lInvite a Player", "",
								"&rInvites a Player to your Party Max. Party Members: &e"
										+ QuestWorld.getPlugin().getConfig().getInt("party.max-members"))
						.get(), event -> {
							Player p2 = (Player) event.getWhoClicked();
							if (party.getSize() >= QuestWorld.getPlugin().getConfig().getInt("party.max-members"))
								PlayerTools.sendTranslation(p2, true, Translation.PARTY_ERROR_FULL);
							else {
								p2.closeInventory();
								PlayerTools.promptInput(p2,
										new SinglePrompt(
												PlayerTools.makeTranslation(true, Translation.PARTY_LEADER_PICKNAME),
												(c, s) -> {
													if (s.equals("cancel()")) {
														PlayerTools.sendTranslation(p2, true, Translation.PARTY_LEADER_PICKCANCEL);
														openPartyMenu(p);
														return true;
													}
													String name = Text.decolor(s).replace("@", "");

													Player player = PlayerTools.getPlayer(name);
													if (player != null) {
														if (QuestWorld.getParty(player) == null) {
															PlayerTools.sendTranslation(p2, true,
																	Translation.PARTY_LEADER_INVITED, name);
															party.invitePlayer(player);
															openPartyMenu(p);
															return true;
														}
														else
															PlayerTools.sendTranslation(p2, true,
																	Translation.PARTY_ERROR_MEMBER, name);
													}
													else {
														PlayerTools.sendTranslation(p2, true,
																Translation.PARTY_ERROR_OFFLINE, name);
													}
													return false;
												}));
							}
						});

				menu.put(17, new ItemBuilder(Material.RED_WOOL)
						.wrapText("&4&lDelete your Party", "", "&rDeletes this Party", "&rBe careful with this Option!")
						.get(), event -> {
							QuestWorld.disbandParty(party);
							openPartyMenu((Player) event.getWhoClicked());
						});
			}
			else {
				menu.put(17, new ItemBuilder(Material.RED_WOOL)
						.wrapText("&4&lLeave your Party", "", "&rLeaves this Party", "&rBe careful with this Option!")
						.get(), event -> {
							Player p2 = (Player) event.getWhoClicked();
							party.playerLeave(p2, LeaveReason.ABANDON);
							openPartyMenu(p2);
						});
			}

			menu.put(13, new ItemBuilder(Material.PLAYER_HEAD)
					.wrapText("&eMember List", "", "&rShows you all Members of this Party").get(), event -> {
						openPartyMembers((Player) event.getWhoClicked());
					});
		}

		menu.openFor(p);
	}

	public static void openCategory(Player p, ICategory category, final boolean back) {
		QuestWorld.getSounds().QUEST_CLICK.playTo(p);
		IPlayerStatus playerStatus = QuestWorld.getPlayerStatus(p);
		playerStatus.update();

		setLastViewed(p, category, back);

		Menu menu = new Menu(1, category.getName());
		ItemBuilder glassPane = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
		PagedMapping view = new PagedMapping(45, 9);

		if (back) {
			int page = PagedMapping.popPage(p);
			PagedMapping.putPage(p, category.getID() / 45);
			PagedMapping.putPage(p, page);

			view.setBackButton(" " + QuestWorld.translate(p, Translation.gui_title), event -> {
				openMainMenu((Player) event.getWhoClicked());
			});
		}

		view.addFrameButton(4, partyMenuItem(p), Buttons.partyMenu(), true);

		for (final IQuest quest : category.getQuests()) {
			if(!quest.isEnabled()) {
				continue;
			}

			IQuest parent = quest.getParent();

			QuestStatus questStatus = playerStatus.getStatus(quest);

			Translation translation;
			String[] keys;

			switch(playerStatus.getStatus(quest)) {
				case LOCKED_WORLD:
					translation = Translation.LOCKED_WORLD;
					keys = new String[]{ p.getWorld().getName() };
					break;

				case LOCKED_NO_PERM: {
					String parts[] = quest.getPermission().split(" ", 2);
					translation = Translation.LOCKED_NO_PERM;
					keys = new String[]{ parts[0], parts[parts.length - 1] };
					break;
				}

				case LOCKED_PARENT:
					translation = Translation.LOCKED_PARENT;
					keys = new String[]{ parent.getName() };
					break;

				case LOCKED_NO_PARTY:
					translation = Translation.LOCKED_NO_PARTY;
					keys = new String[0];
					break;

				case LOCKED_PARTY_SIZE:
					translation = Translation.LOCKED_SMALL_PARTY;
					keys = new String[]{ String.valueOf(quest.getPartySize()) };
					break;

				default:
					translation = null;
					keys = null;
					break;
			}

			if(translation == null) {
				String extra = null;

				if (questStatus == QuestStatus.REWARD_CLAIMABLE) {
					extra = QuestWorld.translate(p, Translation.quests_state_reward_claimable);
				}
				else if (questStatus == QuestStatus.ON_COOLDOWN) {
					extra = QuestWorld.translate(p, Translation.quests_state_cooldown);
				}
				else if (playerStatus.hasFinished(quest)) {
					extra = QuestWorld.translate(p, Translation.quests_state_completed);
				}

				view.addButton(quest.getID(),
						new ItemBuilder(quest.getItem())
								.wrapText(quest.getName(), "", playerStatus.progressString(quest), "",
										"&7" + playerStatus.getProgress(quest) + "/" + quest.getMissions().size()
												+ QuestWorld.translate(p, Translation.quests_tasks_completed),
										(extra == null) ? null : "", extra)
								.get(),
						event -> {
							openQuest((Player) event.getWhoClicked(), quest, back, true);
						}, true);
			}
			else {
				view.addButton(quest.getID(),
						glassPane.wrapText(quest.getName(), "",
								QuestWorld.translate(p, translation, keys)).getNew(),
						null, false);
			}
		}
		view.build(menu, p);
		menu.openFor(p);
	}

	public static void openQuest(final Player p, final IQuest quest, final boolean categoryBack, final boolean back) {
		QuestWorld.getSounds().QUEST_CLICK.playTo(p);
		IPlayerStatus manager = QuestWorld.getPlayerStatus(p);
		manager.update();
		
		setLastViewed(p, quest, categoryBack, back);

		Menu menu = new Menu(3, quest.getName());
		PagedMapping view = new PagedMapping(27, 9);

		if (back) {
			int page = PagedMapping.popPage(p);
			PagedMapping.putPage(p, quest.getID() / 45);
			PagedMapping.putPage(p, page);

			view.setBackButton(" " + quest.getCategory().getName(), event -> {
				openCategory((Player) event.getWhoClicked(), quest.getCategory(), categoryBack);
			});
		}

		// Detect all
		view.addFrameButton(3, new ItemBuilder(Material.CHEST).display("&7Check all Tasks").get(), event -> {
			for (IMission mission : quest.getOrderedMissions()) {
				if (!manager.hasUnlockedTask(mission))
					continue;
				if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE)
						&& quest.getWorldEnabled(p.getWorld().getName())) {
					if (manager.hasCompletedTask(mission))
						continue;

					if (mission.getType() instanceof Manual)
						((Manual) mission.getType()).onManual(p, QuestWorld.getMissionEntry(mission, p));
				}
			}

			openQuest(p, quest, categoryBack, back);
		}, true);

		if (quest.getCooldown() >= 0) {
			String cooldown = quest.getFormattedCooldown();
			if (manager.getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
				// Poor mans "Math.ceil" for integers
				long remaining = (manager.getCooldownEnd(quest) - System.currentTimeMillis() + 59999) / 60 / 1000;
				cooldown = Text.timeFromNum(remaining) + " remaining";
			}
			view.addFrameButton(8, new ItemBuilder(Material.CLOCK)
					.wrapText(QuestWorld.translate(p, Translation.quests_display_cooldown), "", "&b" + cooldown).get(),
					null, false);
		}

		int rewardIndex = 5;
		if (quest.getMoney() > 0 && QuestWorld.getEconomy().isPresent()) {
			view.addFrameButton(rewardIndex, new ItemBuilder(Material.GOLD_INGOT)
					.wrapText(QuestWorld.translate(p, Translation.quests_display_monetary), "", "&6$" + quest.getMoney())
					.get(), null, false);
			rewardIndex++;
		}

		if (quest.getXP() > 0) {
			view.addFrameButton(rewardIndex, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
					.wrapText(QuestWorld.translate(p, Translation.quests_display_exp), "", "&a" + quest.getXP() + " Level")
					.get(), null, false);
			rewardIndex++;
		}

		ItemStack glassLocked = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
				.wrapText("&7&kSOMEWEIRDMISSION", "", QuestWorld.translate(p, Translation.task_locked)).get();
		
		ItemStack glassClaimable = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE)
				.wrapText(QuestWorld.translate(p, Translation.quests_state_reward_claim)).get();
		
		ItemStack glassCooldown = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE)
				.wrapText(QuestWorld.translate(p, Translation.quests_state_cooldown)).get();
		
		ItemStack glassInactive = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
				.wrapText(QuestWorld.translate(p, Translation.quests_display_rewards)).get();
		
		int index = 0;
		for (final IMission mission : quest.getOrderedMissions()) {			
			ItemStack item = glassLocked;
			
			if (manager.hasUnlockedTask(mission)) {
				ItemBuilder entryItem = new ItemBuilder(mission.getDisplayItem());
				int current = manager.getProgress(mission);
				int total = mission.getAmount();
				String progress = Text.progressBar(current, total, mission.getType().progressString(current, total));

				if (mission.getType() instanceof Manual) {
					entryItem.wrapText(mission.getText(), "", progress, "", ((Manual) mission.getType()).getLabel());
				}
				else
					entryItem.wrapText(mission.getText(), "", progress);

				item = entryItem.get();
			}

			view.addButton(index, item, event -> {
				if (!manager.hasUnlockedTask(mission))
					return;
				if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE)
						&& quest.getWorldEnabled(p.getWorld().getName())) {
					if (manager.hasCompletedTask(mission))
						return;

					if (mission.getType() instanceof Manual) {
						((Manual) mission.getType()).onManual(p, QuestWorld.getMissionEntry(mission, p));
						openQuest(p, quest, categoryBack, back);
					}
				}
			}, true);
			index++;
		}
		
		int offset = 9 + 9 * Math.min(((quest.getMissions().size() + 8) / 9), 3);

		for (int i = 0; i < 9; i++) {
			if (manager.getStatus(quest).equals(QuestStatus.REWARD_CLAIMABLE)) {
				menu.put(i + offset, glassClaimable, event -> {
							quest.completeFor(p);
							// TODO QuestWorld.getSounds().muteNext();
							PagedMapping.putPage(p, view.getCurrentPage());
							openQuest(p, quest, categoryBack, back);
						});
			}
			else if (manager.getStatus(quest).equals(QuestStatus.ON_COOLDOWN)) {
				menu.put(i + offset, glassCooldown, null);
			}
			else {
				menu.put(i + offset, glassInactive, null);
			}
		}

		int slot = offset + 9;
		for (ItemStack reward : quest.getRewards()) {
			menu.put(slot, reward, null);
			slot++;
		}
		
		view.build(menu, p);
		menu.openFor(p);
	}

	/*
	 * 
	 * Quest Editor
	 * 
	 */
	public static void openCategoryList(Player p) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		final Menu menu = new Menu(6, "&3Categories");

		ItemBuilder defaultItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
				.display("&7> Create category");

		PagedMapping view = new PagedMapping(45);
		view.reserve(1);

		for (ICategory category : QuestWorld.getFacade().getCategories()) {
			String[] lore = { category.getName(), "", "&rLeft click: &eOpen quest list",
					"&rShift left click: &eOpen category editor", "&rRight click: &eRemove category" };
			int quests = category.getQuests().size();
			if (quests > 0) {
				String[] lines = new String[lore.length + Math.min(quests, 6)];
				lines[0] = category.getName();

				int j = 1;
				for (IQuest q : category.getQuests()) {
					if (j > 5) {
						lines[j++] = "&7&oand " + (quests - 5) + " more...";
						break;
					}
					lines[j++] = "&7- " + q.getName();
				}

				for (int k = 0; k < 4; ++k)
					lines[k + j] = lore[k + 1];

				lore = lines;
			}

			view.addButton(category.getID(), new ItemBuilder(category.getItem()).wrapText(lore).get(),
					Buttons.onCategory(category), true);

			view.reserve(1);
		}

		for (int i = 0; i < view.getCapacity(); ++i)
			if (!view.hasButton(i))
				view.addButton(i, defaultItem.get(), Buttons.newCategory(i), true);

		view.build(menu, p);
		menu.openFor(p);
	}

	public static void openQuestList(Player p, final ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		final Menu menu = new LinkedMenu(6, "&3Quests", category, true);

		ItemBuilder defaultItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
				.display("&7> Create quest");

		PagedMapping view = new PagedMapping(45);
		view.reserve(1);
		view.setBackButton(" &3Categories", event -> {
			openCategoryList((Player) event.getWhoClicked());
		});

		view.addFrameButton(4, new ItemBuilder(Material.WRITABLE_BOOK).display("&3Category editor").get(), event -> {
			openCategoryEditor(p, category);
		}, true);

		for (IQuest quest : category.getQuests()) {
			String[] lore = { quest.getName(), "", "&rLeft click: &eOpen quest editor",
					"&rRight click: &eRemove quest" };

			int missions = quest.getMissions().size();
			if (missions > 0) {
				String[] lines = new String[lore.length + Math.min(missions, 6)];
				lines[0] = quest.getName();

				int j = 1;
				for (IMission m : quest.getOrderedMissions()) {
					if (j > 5) {
						lines[j++] = "&7&oand " + (missions - 5) + " more...";
						break;
					}
					lines[j++] = "&7- " + m.getText();

				}

				for (int k = 0; k < 3; ++k)
					lines[k + j] = lore[k + 1];

				lore = lines;
			}

			view.addButton(quest.getID(), new ItemBuilder(quest.getItem()).wrapText(lore).get(), Buttons.onQuest(quest),
					true);

			view.reserve(1);
		}

		for (int i = 0; i < view.getCapacity(); ++i)
			if (!view.hasButton(i))
				view.addButton(i, defaultItem.getNew(), Buttons.newQuest(category, i), true);

		view.build(menu, p);
		menu.openFor(p);
	}

	public static void openCategoryEditor(Player p, final ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		final Menu menu = new LinkedMenu(2, "&3Category editor", category, true);
		ICategoryState changes = category.getState();

		menu.put(0, Proto.MAP_BACK.get().wrapLore(" &3Categories").get(), event -> {
			openCategoryList((Player) event.getWhoClicked());
		});

		menu.put(4, new ItemBuilder(Material.WRITABLE_BOOK).display("&3Quest list").get(), event -> {
			openQuestList(p, category);
		});

		menu.put(9, new ItemBuilder(category.getItem())
				.wrapText(category.getName(), "", "&e> Click to set the display item").get(), event -> {
					Player p2 = (Player) event.getWhoClicked();
					ItemStack hand = p2.getInventory().getItemInMainHand();
					if (hand != null) {
						changes.setItem(hand);
						changes.apply();
						openCategoryEditor(p2, category);
					}
				});

		menu.put(10, new ItemBuilder(Material.NAME_TAG)
				.wrapText(category.getName(), "", "&e> Click to set category name").get(), event -> {
					Player p2 = (Player) event.getWhoClicked();
					p2.closeInventory();
					PlayerTools.promptInput(p2, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.CATEGORY_NAME_EDIT, category.getName()),
							(c, s) -> {
								String oldName = category.getName();
								s = Text.deserializeNewline(Text.colorize(s));
								changes.setName(s);
								if (changes.apply())
									PlayerTools.sendTranslation(p2, true, Translation.CATEGORY_NAME_SET, s, oldName);

								QuestBook.openCategoryEditor(p2, category);
								return true;
							}));
				});

		IQuest parent = category.getParent();
		menu.put(11,
				new ItemBuilder(Material.WRITABLE_BOOK)
						.wrapText("&7Requirement: &r&o" + (parent != null ? parent.getName() : "-none-"), "",
								"&rLeft click: &eOpen requirement selector", "&rRight click: &eRemove requirement")
						.get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					if (event.isRightClick()) {
						changes.setParent(null);
						if (changes.apply()) {

						}
						openCategoryEditor(p2, category);
					}
					else {
						PagedMapping.putPage(p2, 0);
						QBDialogue.openRequirementCategories(p2, category);
					}
				});

		menu.put(12,
				new ItemBuilder(Material.NAME_TAG).wrapText(
						"&7Permission: &r"
								+ (category.getPermission().equals("") ? "&o-none-" : category.getPermission()),
						"", "&e> Click to change the required permission").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					p2.closeInventory();
					PlayerTools.promptInput(p2, new SinglePrompt(PlayerTools.makeTranslation(true,
							Translation.CATEGORY_PERM_EDIT, category.getName(), category.getPermission()), (c, s) -> {
								String permission = s.equalsIgnoreCase("none") ? "" : s;
								String oldPerm = category.getPermission();
								changes.setPermission(permission);
								if (changes.apply())
									PlayerTools.sendTranslation(p2, true, Translation.CATEGORY_PERM_SET,
											category.getName(), s, oldPerm);

								QuestBook.openCategoryEditor(p2, category);
								return true;
							}));
				});

		menu.put(13,
				new ItemBuilder(Material.GOLDEN_CARROT)
						.wrapText("&7Show in quest book: " + (!category.isHidden() ? "&2&l\u2714" : "&4&l\u2718"), "",
								"&e> Toggle category visibility")
						.get(),
				event -> {
					changes.setHidden(!category.isHidden());
					if (changes.apply()) {
					}
					openCategoryEditor((Player) event.getWhoClicked(), category);
				});

		menu.put(14, new ItemBuilder(Material.GRASS).wrapText("&7World blacklist", "", "&e> Click open world selector")
				.get(), event -> {
					openWorldEditor((Player) event.getWhoClicked(), category);
				});

		menu.put(17, new ItemBuilder(Material.RED_WOOL)
				.wrapText("&4Reset progress", "", "&e> Click to clear all player progress for this category").get(),
				event -> {
					// TODO: Destructive action warning
					QuestWorld.getFacade().clearAllUserData(category);

					QuestWorld.getSounds().DESTRUCTIVE_CLICK.playTo((Player) event.getWhoClicked());
				});

		menu.openFor(p);
	}
	
	public static void openMissionList(Player p, final IQuest quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		final Menu menu = new LinkedMenu(6, "&3Missions", quest, true);

		ItemStack defaultItem = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
				.display("&7> Create mission").get();

		PagedMapping view = new PagedMapping(45);
		view.reserve(1);
		view.setBackButton(" &3Quests", event -> {
			openQuestList((Player) event.getWhoClicked(), quest.getCategory());
		});

		view.addFrameButton(4, new ItemBuilder(Material.WRITABLE_BOOK).display("&3Quest editor").get(), event -> {
			openQuestEditor(p, quest);
		}, true);
		
		///
		IQuestState changes = quest.getState();

		// TODO: Mission move
		for (IMission mission : quest.getMissions()) {
			// TODO: Hack to maybe deal with out of order quests
			int missionIndex = mission.getIndex();
			view.addButton(missionIndex,
					new ItemBuilder(mission.getType().getSelectorItem()).flagAll()
							.wrapText(mission.getText(), "", "&rLeft click: &eOpen mission editor",
									"&rRight click: &eRemove mission"/*
																		 * , "&rShift right click: &eMove mission"
																		 */)
							.get(),
					event -> {
						Player p2 = (Player) event.getWhoClicked();
						if (!event.isRightClick())
							openQuestMissionEditor(p2, mission);
						// else if(event.isShiftClick())
						// openMissionMove(p, quest, mission);
						else
							QBDialogue.openDeletionConfirmation(p2, mission);
					}, true);
			
			view.reserve(1);
		}
		
		for (int i = 0; i < view.getCapacity(); ++i) {
			if(!view.hasButton(i)) {
				int index = i;
				
				view.addButton(i, defaultItem, event -> {
							changes.addMission(index);
	
							changes.apply();
							openMissionList((Player) event.getWhoClicked(), quest);
						}, true);
			}
		}

		view.build(menu, p);
		menu.openFor(p);
	}

	public static void openQuestEditor(Player p, final IQuest quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		final Menu menu = new LinkedMenu(6, "&3Quest editor", quest, true);
		IQuestState changes = quest.getState();

		menu.put(0, ItemBuilder.Proto.MAP_BACK.get().wrapLore(" &3Quests").get(), event -> {
			openQuestList((Player) event.getWhoClicked(), quest.getCategory());
		});

		menu.put(4, new ItemBuilder(Material.WRITABLE_BOOK).display("&3Mission list").get(), event -> {
			openMissionList(p, quest);
		});

		menu.put(9, new ItemBuilder(quest.getItem()).wrapText(quest.getName(), "", "&e> Click to set the display item")
				.get(), event -> {
					Player p2 = (Player) event.getWhoClicked();
					ItemStack mainItem = p2.getInventory().getItemInMainHand();
					if (mainItem != null) {
						changes.setItem(mainItem);
						changes.apply();

						openQuestEditor(p2, quest);
					}
				});

		menu.put(10,
				new ItemBuilder(Material.NAME_TAG).wrapText(quest.getName(), "", "&e> Click to set quest name").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					p2.closeInventory();
					PlayerTools.promptInput(p2, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.QUEST_NAME_EDIT, quest.getName()), (c, s) -> {
								String oldName = quest.getName();
								s = Text.deserializeNewline(Text.colorize(s));
								changes.setName(s);
								if (changes.apply())
									PlayerTools.sendTranslation(p2, true, Translation.QUEST_NAME_SET, s, oldName);

								openQuestEditor(p2, quest);
								return true;
							}));
				});

		menu.put(11,
				new ItemBuilder(Material.CHEST)
						.wrapText("&7Item reward", "", "&e> Click to set the reward to the items in your hotbar").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					changes.setItemRewards(p2);
					changes.apply();

					openQuestEditor(p2, quest);
				});

		menu.put(12,
				new ItemBuilder(Material.CLOCK)
						.wrapText("&7Cooldown: &b" + quest.getFormattedCooldown(), "", "&rLeft click: &e+1m",
								"&rRight click: &e-1m", "&rShift left click: &e+1h", "&rShift right click: &e-1h")
						.get(),
				event -> {
					// Work with raw cooldowns so -1 is actually -1
					long cooldown = quest.getRawCooldown();
					long delta = IQuest.COOLDOWN_SCALE;
					if (event.isShiftClick())
						delta *= 60;
					if (event.isRightClick())
						delta = -delta;

					// Force a step at 0, so you can't jump from 59 -> -1 or -1 -> 59
					if (cooldown + delta < 0) {
						if (cooldown <= 0)
							cooldown = -1;
						else
							cooldown = 0;
					}
					else if (cooldown == -1)
						cooldown = 0;
					else
						cooldown += delta;

					changes.setRawCooldown(cooldown);
					changes.apply();

					QuestBook.openQuestEditor((Player) event.getWhoClicked(), quest);
				});

		if (QuestWorld.getEconomy().isPresent())
			menu.put(13,
					new ItemBuilder(Material.GOLD_INGOT)
							.wrapText("&7Monetary reward: &6$" + quest.getMoney(), "", "&rLeft click: &e+1",
									"&rRight click: &e-1", "&rShift left click: &e+100", "&rShift right click: &e-100")
							.get(),
					event -> {
						int money = MissionButton.clickNumber(quest.getMoney(), 100, event);
						if (money < 0)
							money = 0;
						changes.setMoney(money);
						changes.apply();
						openQuestEditor((Player) event.getWhoClicked(), quest);
					});

		menu.put(14,
				new ItemBuilder(Material.EXPERIENCE_BOTTLE)
						.wrapText("&7Level reward: &b" + quest.getXP(), "", "&rLeft click: &e+1", "&rRight click: &e-1",
								"&rShift left click: &e+10", "&rShift right click: &e-10")
						.get(),
				event -> {
					int xp = MissionButton.clickNumber(quest.getXP(), 10, event);
					if (xp < 0)
						xp = 0;
					changes.setXP(xp);
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				});

		IQuest parent = quest.getParent();
		menu.put(15,
				new ItemBuilder(Material.WRITABLE_BOOK)
						.wrapText("&7Requirement: &r&o" + (parent != null ? parent.getName() : "-none-"), "",
								"&rLeft click: &eOpen requirement selector", "&rRight click: &eRemove requirement")
						.get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					if (event.isRightClick()) {
						changes.setParent(null);
						changes.apply();
						openQuestEditor(p2, quest);
					}
					else {
						PagedMapping.putPage(p2, 0);
						QBDialogue.openRequirementCategories(p2, quest);
					}
				});

		menu.put(16, new ItemBuilder(Material.COMMAND_BLOCK)
				.wrapText("&7Command rewards", "", "&e> Click to open command editor").get(), event -> {
					Player p2 = (Player) event.getWhoClicked();
					p2.closeInventory();
					QBDialogue.openCommandEditor(p2, quest);
				});

		menu.put(17,
				new ItemBuilder(Material.NAME_TAG).wrapText(
						"&7Permission: &r" + (quest.getPermission().equals("") ? "&o-none-" : quest.getPermission()),
						"", "&e> Click to change the required permission").get(),
				event -> {
					Player p2 = (Player) event.getWhoClicked();
					p2.closeInventory();
					PlayerTools.promptInput(p2, new SinglePrompt(PlayerTools.makeTranslation(true,
							Translation.QUEST_PERM_EDIT, quest.getName(), quest.getPermission()), (c, s) -> {
								String permission = s.equalsIgnoreCase("none") ? "" : s;
								String oldPerm = quest.getPermission();
								changes.setPermission(permission);
								if (changes.apply())
									PlayerTools.sendTranslation(p2, true, Translation.QUEST_PERM_SET, quest.getName(),
											s, oldPerm);

								openQuestEditor(p2, quest);
								return true;
							}));
				});

		menu.put(18,
				new ItemBuilder(Material.FIREWORK_ROCKET)
						.wrapText("&7Party progress: " + (quest.supportsParties() ? "&2&l\u2714" : "&4&l\u2718"), "",
								"&e> Toggle whether all party members get progress when a single member makes progress")
						.get(),
				event -> {
					changes.setPartySupport(!quest.supportsParties());
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				});

		menu.put(19,
				new ItemBuilder(Material.COMMAND_BLOCK)
						.wrapText("&7Ordered completion: " + (quest.getOrdered() ? "&2&l\u2714" : "&4&l\u2718"), "",
								"&e> Toggle whether tasks must be completed in order")
						.get(),
				event -> {
					changes.setOrdered(!quest.getOrdered());
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				});

		menu.put(20,
				new ItemBuilder(Material.CHEST)
						.wrapText("&7Auto-claim rewards: " + (quest.getAutoClaimed() ? "&2&l\u2714" : "&4&l\u2718"), "",
								"&e> Toggle whether this quest's rewards will be"
										+ " automatically given or have to be claimed manually")
						.get(),
				event -> {
					changes.setAutoClaim(!changes.getAutoClaimed());
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				});

		menu.put(21, new ItemBuilder(Material.GRASS)
				.wrapText("&7World blacklist", "", "&e> Click to open world selector").get(), event -> {
					openWorldSelector((Player) event.getWhoClicked(), quest);
				});

		String wtfString = "&7Party size: " + (quest.getPartySize() < 1 ? "&4Parties prohibited"
				: (quest.getPartySize() == 1 ? ("&aAny size") : ("&e" + quest.getPartySize() + " members")));
		menu.put(22, new ItemBuilder(Material.FIREWORK_ROCKET).wrapText(wtfString, "", "&e> Click to set minimum party size",
				"", "&rLeft click: &e+1", "&rRight click: &e-1").get(), event -> {
					int size = MissionButton.clickNumber(quest.getPartySize(), 1, event);
					if (size < 0)
						size = 0;
					changes.setPartySize(size);
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				});
		
		menu.put(24, new ItemBuilder(Material.EMERALD).wrapText(
					"&7Enabled: " + Text.booleanBadge(quest.isEnabled()),
					"&e> Toggles hiding and disabling the quest"
				).get(), event -> {
					changes.setEnabled(!quest.isEnabled());
					changes.apply();
					openQuestEditor((Player) event.getWhoClicked(), quest);
				});

		menu.put(26, new ItemBuilder(Material.RED_WOOL)
				.wrapText("&4Reset progress", "", "&e> Click to clear all player progress for this quest").get(),
				event -> {
					QuestWorld.getFacade().clearAllUserData(quest);
					QuestWorld.getSounds().DESTRUCTIVE_CLICK.playTo((Player) event.getWhoClicked());
				});

		int index = 36;
		for (ItemStack reward : quest.getRewards()) {
			menu.put(index, reward, null);
			index++;
		}

		menu.openFor(p);
	}

	/*
	 * public static void openMissionMove(Player p, IQuest quest, IMission from) {
	 * QuestWorld.getSounds().EDITOR_CLICK.playTo(p); Menu menu = new Menu(2,
	 * "&3Mission order");
	 * 
	 * menu.put(0,
	 * ItemBuilder.Proto.MAP_BACK.get().wrapLore(" &3QMission order").get(), event
	 * -> { openQuestEditor((Player) event.getWhoClicked(), quest); });
	 * 
	 * for(int i = 0; i < 9; ++i) { int index = i + 54; menu.put(i + 9, new
	 * ItemBuilder(Material.STAINED_GLASS_PANE).color(DyeColor.RED).display(
	 * "&7Empty").lore( "", "&e> Move here").get(), event -> { IMissionState state =
	 * from.getState(); state.setIndex(index); state.apply(); openQuestEditor(p,
	 * quest); }); }
	 * 
	 * for (IMission to : quest.getMissions()) { int index = to.getIndex();
	 * menu.put(index + 9, new
	 * ItemBuilder(to.getType().getSelectorItem()).flagAll().wrapText( to.getText(),
	 * "", "&e> Swap missions").get(), event -> { IMissionState toState =
	 * to.getState(); IMissionState fromState = from.getState();
	 * 
	 * toState.setIndex(from.getIndex()); fromState.setIndex(index);
	 * toState.apply(); fromState.apply(); openQuestEditor(p, quest); } ); }
	 * 
	 * menu.openFor(p); }
	 */

	public static void openWorldSelector(Player p, final IQuest quest) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		final Menu menu = new LinkedMenu(2, "&3World selector", quest, true);

		menu.put(0, ItemBuilder.Proto.MAP_BACK.get().wrapLore(" &3Quest editor").get(), event -> {
			openQuestEditor((Player) event.getWhoClicked(), quest);
		});

		int index = 9;
		for (final World world : Bukkit.getWorlds()) {
			menu.put(index, new ItemBuilder(Material.GRASS).display("&7" + Text.niceName(world.getName()) + ": "
					+ (quest.getWorldEnabled(world.getName()) ? "&2&l\u2714" : "&4&l\u2718")).get(), event -> {
						IQuestState changes = quest.getState();
						changes.toggleWorld(world.getName());
						if (changes.apply()) {
						}

						openWorldSelector((Player) event.getWhoClicked(), quest);
					});
			index++;
		}

		menu.openFor(p);
	}

	public static void openWorldEditor(Player p, final ICategory category) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		final Menu menu = new LinkedMenu(2, "&3World selector", category, true);

		menu.put(0, ItemBuilder.Proto.MAP_BACK.get().wrapLore(" &3Category editor").get(), event -> {
			openCategoryEditor((Player) event.getWhoClicked(), category);
		});

		int index = 9;
		for (final World world : Bukkit.getWorlds()) {
			menu.put(index,
					new ItemBuilder(Material.GRASS).display("&7" + Text.niceName(world.getName()) + ": "
							+ (category.isWorldEnabled(world.getName()) ? "&2&l\u2714" : "&4&l\u2718")).get(),
					event -> {
						ICategoryState changes = category.getState();
						changes.toggleWorld(world.getName());
						if (changes.apply()) {
						}

						openWorldEditor((Player) event.getWhoClicked(), category);
					});
			index++;
		}

		menu.openFor(p);
	}

	public static void openQuestMissionEditor(Player p, final IMission mission) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		Menu menu = new LinkedMenu(2, "&3Mission editor", mission, true);

		menu.put(0, ItemBuilder.Proto.MAP_BACK.get().wrapLore(" &3Missions").get(), e -> {
			openMissionList(p, mission.getQuest());
		});

		// Mission types now handle their own menu data!
		mission.getType().buildMenu(mission.getState(), menu);

		ItemStack missionSelector = new ItemBuilder(mission.getType().getSelectorItem()).flagAll().wrapText(
				"&7" + Text.niceName(mission.getType().toString()), "", "&e> Click to change the mission type").get();

		menu.put(9, missionSelector, e -> {
			openMissionSelector(p, mission);
		});

		menu.openFor(p);
	}

	public static void openMissionSelector(Player p, IMission mission) {
		QuestWorld.getSounds().EDITOR_CLICK.playTo(p);

		IMissionState changes = mission.getState();
		final Menu menu = new LinkedMenu(3, "&3Mission selector", mission, true);

		PagedMapping.putPage(p, 0);

		PagedMapping view = new PagedMapping(45, 9);
		view.setBackButton(" &3Mission editor", event -> {
			openQuestMissionEditor((Player) event.getWhoClicked(), mission);
		});

		ArrayList<MissionType> types = new ArrayList<>(QuestWorld.getMissionTypes().values());
		Collections.sort(types, (m1, m2) -> m1.toString().compareToIgnoreCase(m2.toString()));

		int i = 0;
		for (MissionType type : types) {
			String name = Text.niceName(type.getName());
			view.addButton(i, new ItemBuilder(type.getSelectorItem()).display("&7" + name).flagAll().get(), event -> {
				changes.setType(type);
				MissionButton.apply(event, changes);
			}, false);
			++i;
		}
		view.build(menu, p);

		menu.openFor(p);
	}
}
