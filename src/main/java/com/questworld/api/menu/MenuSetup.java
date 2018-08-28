package com.questworld.api.menu;

import com.questworld.api.QuestWorld;
import com.questworld.api.SinglePrompt;
import com.questworld.api.Translation;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IQuest;
import com.questworld.api.contract.IQuestState;
import com.questworld.manager.DataEventTransformer;
import com.questworld.manager.MenuRegistry;
import com.questworld.util.PlayerTools;
import com.questworld.util.Text;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MenuSetup {
	private String namespace = "";
	private MenuRegistry registry = new MenuRegistry();

	public MenuSetup() {
		registerQuestEditor();
	}

	public MenuRegistry getRegistry() {
		return registry;
	}

	private void namespace() {
		namespace = "";
	}

	private void namespace(String key) {
		namespace = key;

		if(key.length() > 0) {
			namespace += ':';
		}
	}

	private void register(String key, DataEventTransformer function) {

		registry.register(namespace + key, function);
	}

	private void registerQuestEditor() {
		namespace("editor:quest");

		register("back", object -> {
			IQuestState changes = (IQuestState) object;
			ICategory category = changes.getSource().getCategory();

			return event -> {
				QuestBook.openQuestList((Player) event.getWhoClicked(), category);
			};
		});

		register("set_display", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				Player player = (Player) event.getWhoClicked();
				ItemStack mainItem = player.getInventory().getItemInMainHand();
				if (mainItem != null) {
					changes.setItem(mainItem);
					changes.apply();

					QuestBook.openQuestEditor(player, changes.getSource());
				}
			};
		});

		register("set_name", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				Player player = (Player) event.getWhoClicked();
				player.closeInventory();
				PlayerTools.promptInput(player, new SinglePrompt(
						PlayerTools.makeTranslation(true, Translation.QUEST_NAME_EDIT, changes.getName()), (c, s) -> {
							String oldName = changes.getName();
							s = Text.deserializeNewline(Text.colorize(s));
							changes.setName(s);
							if (changes.apply())
								PlayerTools.sendTranslation(player, true, Translation.QUEST_NAME_SET, s, oldName);

							QuestBook.openQuestEditor(player, changes.getSource());
							return true;
						}));
			};
		});

		register("reward:item", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				Player player = (Player) event.getWhoClicked();
				changes.setItemRewards(player);
				changes.apply();

				QuestBook.openQuestEditor(player, changes.getSource());
			};
		});

		register("set_cooldown", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				// Work with raw cooldowns so -1 is actually -1
				long cooldown = changes.getRawCooldown();
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

				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("reward:money", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				int money = MissionButton.clickNumber(changes.getMoney(), 100, event);
				if (money < 0)
					money = 0;
				changes.setMoney(money);
				changes.apply();
				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("reward:xp", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				int xp = MissionButton.clickNumber(changes.getXP(), 10, event);
				if (xp < 0)
					xp = 0;
				changes.setXP(xp);
				changes.apply();
				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("requirement:quest", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				Player p2 = (Player) event.getWhoClicked();
				if (event.isRightClick()) {
					changes.setParent(null);
					changes.apply();
					QuestBook.openQuestEditor(p2, changes.getSource());
				}
				else {
					PagedMapping.putPage(p2, 0);
					QBDialogue.openRequirementCategories(p2, changes.getSource());
				}
			};
		});

		register("reward:command", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				Player player = (Player) event.getWhoClicked();
				player.closeInventory();
				QBDialogue.openCommandEditor(player, changes.getSource());
			};
		});

		register("requirement:permission", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				Player player = (Player) event.getWhoClicked();
				player.closeInventory();
				PlayerTools.promptInput(player, new SinglePrompt(PlayerTools.makeTranslation(true,
						Translation.QUEST_PERM_EDIT, changes.getName(), changes.getPermission()), (c, s) -> {
							String permission = s.equalsIgnoreCase("none") ? "" : s;
							String oldPerm = changes.getPermission();
							changes.setPermission(permission);
							if (changes.apply())
								PlayerTools.sendTranslation(player, true, Translation.QUEST_PERM_SET, changes.getName(),
										s, oldPerm);

							QuestBook.openQuestEditor(player, changes.getSource());
							return true;
						}));
			};
		});

		register("set_party_progress", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				changes.setPartySupport(!changes.supportsParties());
				changes.apply();
				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("set_ordered", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				changes.setOrdered(!changes.getOrdered());
				changes.apply();
				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("set_autoclaim", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				changes.setAutoClaim(!changes.getAutoClaimed());
				changes.apply();
				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("requirement:world", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				QuestBook.openWorldSelector((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("requirement:party_size", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				int size = MissionButton.clickNumber(changes.getPartySize(), 1, event);
				if (size < 0)
					size = 0;
				changes.setPartySize(size);
				changes.apply();
				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("reset", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				QuestWorld.getFacade().clearAllUserData(changes.getSource());
				QuestWorld.getSounds().DESTRUCTIVE_CLICK.playTo((Player) event.getWhoClicked());
			};
		});

		register("new_mission", object -> {
			IQuestState changes = (IQuestState) object;

			return event -> {
				// TODO magic number
				changes.addMission(event.getSlot() - 45);
				changes.apply();
				QuestBook.openQuestEditor((Player) event.getWhoClicked(), changes.getSource());
			};
		});

		register("open_mission", object -> {
			IMission mission = (IMission) object;

			return event -> {
				Player player = (Player) event.getWhoClicked();
				if (!event.isRightClick())
					QuestBook.openQuestMissionEditor(player, mission);
				// else if(event.isShiftClick())
				// openMissionMove(p, quest, mission);
				else
					QBDialogue.openDeletionConfirmation(player, mission);
			};
		});

		namespace();
	}
}
