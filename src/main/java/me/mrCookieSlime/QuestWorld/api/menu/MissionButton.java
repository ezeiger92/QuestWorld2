package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.quest.QBDialogue;
import me.mrCookieSlime.QuestWorld.quest.QuestBook;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

public class MissionButton {
	public static MenuData item(IMissionWrite changes) {
		return simpleButton(changes,
				new ItemBuilder(changes.getMissionItem()).lore(
						"",
						"&e> Click to change the Item to",
						"&ethe Item you are currently holding").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
					ItemStack hand = p.getInventory().getItemInMainHand();
					if(hand != null && hand.getType() != Material.AIR)
						changes.setItem(hand.clone());
				}
		);
	}

	public static MenuData amount(IMissionWrite changes) {
		return amount(changes, 16);
	}
	
	public static MenuData amount(IMissionWrite changes, int groupSize) {
		return simpleButton(changes,
				new ItemBuilder(Material.REDSTONE).display("&7Amount: &b" + changes.getAmount()).lore(
						"",
						"&rLeft Click: &e+1",
						"&rRight Click: &e-1",
						"&rShift + Left Click: &e+"+groupSize,
						"&rShift + Right Click: &e-"+groupSize).get(),
				event -> {
					int amt = clickNumber(changes.getAmount(), groupSize, event);
					changes.setAmount(Math.max(amt, 1));
				}
		);
	}
	
	public static MenuData entity(IMissionWrite changes) {
		EntityType entity = changes.getEntity();
		
		return new MenuData(
				new ItemBuilder(EntityTools.getEntityDisplay(entity))
				.display("&7Entity Type: &r" + Text.niceName(entity.name())).lore(
						"",
						"&e> Click to change the Entity").get(),
				event -> {
					QBDialogue.openQuestMissionEntityEditor((Player)event.getWhoClicked(), changes);
				}
		);
	}
	
	public static MenuData location(IMissionWrite changes) {
		return simpleButton(changes,
				new ItemBuilder(changes.getDisplayItem()).lore(
						"",
						"&e> Click to change the Location",
						"&eto your current Position").get(),
				event -> {
					changes.setLocation((Player)event.getWhoClicked());
				}
		);
	}
	
	public static MenuData entityName(IMissionWrite changes) {
		return new MenuData(
				new ItemBuilder(Material.NAME_TAG)
				.display("&r" + changes.getCustomString()).lore(
						"",
						"&e> Click to change the Name").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();

					PlayerTools.closeInventoryWithEvent(p);
					PlayerTools.promptInput(p, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.KILLMISSION_NAME_EDIT),
							(c,s) -> {
								changes.setCustomString(Text.colorize(s));
								if(changes.apply()) {
									PlayerTools.sendTranslation(p, true, Translation.KILLMISSION_NAME_SET);
								}
								QuestBook.openQuestMissionEditor(p, changes);
								return true;
							}
					));
				}
		);
	}
	
	public static MenuData missionName(IMissionWrite changes) {
		return new MenuData(
				new ItemBuilder(Material.NAME_TAG).display("").lore(
						changes.getText(),
						"",
						"&rLeft Click: Edit Mission Name",
						"&rRight Click: Reset Mission Name").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
			
					if(event.getClick().isRightClick()) {
						changes.setDisplayName(null);
						apply(event, changes);
					}
					else {
						PlayerTools.closeInventoryWithEvent(p);
						PlayerTools.promptInput(p, new SinglePrompt(
								PlayerTools.makeTranslation(true, Translation.MISSION_NAME_EDIT),
								(c,s) -> {
									changes.setDisplayName(s);
									if(changes.apply()) {
										PlayerTools.sendTranslation(p, true, Translation.MISSION_NAME_SET);
										QuestBook.openQuestMissionEditor(p, changes);
									}
									return true;
								}
						));
					}
				}
		);
	}

	public static MenuData timeframe(IMissionWrite changes) {
		return simpleButton(changes,
				new ItemBuilder(Material.WATCH)
				.display("&7Complete Mission within: &b" + Text.timeFromNum(changes.getTimeframe())).lore(
						"",
						"&rLeft Click: &e+1m",
						"&rRight Click: &e-1m",
						"&rShift + Left Click: &e+1h",
						"&rShift + Right Click: &e-1h").get(),
				event -> {
					int amt = clickNumber(changes.getTimeframe(), 60, event);
					changes.setTimeframe(Math.max(amt, 0));
				}
		);
	}
	
	public static MenuData deathReset(IMissionWrite changes) {
		return simpleButton(changes,
				new ItemBuilder(Material.SKULL_ITEM)
				.display("&7Resets on Death: " + (changes.resetsonDeath() ? "&2&l\u2714": "&4&l\u2718")).lore(
						"",
						"&e> Click to change whether this Mission's Progress",
						"&eresets when a Player dies").get(),
				event -> {
					changes.setDeathReset(!changes.resetsonDeath());
				}
		);
	}
	
	public static MenuData spawnersAllowed(IMissionWrite changes) {
		return simpleButton(changes,
				new ItemBuilder(Material.MOB_SPAWNER)
				.display("&7Allow Mobs from Spawners: " + (changes.acceptsSpawners() ? "&2&l\u2714": "&4&l\u2718")).lore(
						"",
						"&e> Click to change whether this Mission will",
						"&ealso count Mobs which were spawned by a Mob Spawner").get(),
				event -> {
					changes.setSpawnerSupport(!changes.acceptsSpawners());
				}
		);
	}
	
	public static MenuData dialogue(IMissionWrite changes) {
		return new MenuData(
				new ItemBuilder(Material.PAPER).display("&rDialogue").lore(
						"",
						"&rLeft Click: Edit the Dialogue",
						"&rRight Click: Dialogue Preview").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
					
					if (event.getClick().isRightClick()) {
						if (changes.getDialogue().isEmpty())
							p.sendMessage(Text.colorize("&4No Dialogue found!"));
						else
							PlayerManager.sendQuestDialogue(p, changes, changes.getDialogue().iterator());
					}
					else
						((MissionChange)changes).getSource().setupDialogue(p);
						
					PlayerTools.closeInventoryWithEvent(p);
				}
		);
	}
	
	public static void apply(InventoryClickEvent event, IMissionWrite changes) {
		if(changes.apply()) {
			// TODO remove MissionChange and QuestBook from here, somehow
			QuestBook.openQuestMissionEditor((Player) event.getWhoClicked(), changes);
		}
	}

	public static MenuData simpleButton(IMissionWrite changes, ItemStack item, Consumer<InventoryClickEvent> action)  {
		return new MenuData(item, action.andThen(event -> apply(event, changes)));
	}
	
	public static int clickNumber(int initial, int groupSize, InventoryClickEvent event) {
		switch(event.getClick()) {
		case RIGHT: initial -= 1; break;
		case SHIFT_RIGHT: initial -= groupSize; break;
		case LEFT: initial += 1; break;
		case SHIFT_LEFT: initial += groupSize; break;
		case NUMBER_KEY: initial = initial * 10 + event.getHotbarButton() + 1; break;
		case DROP: initial /= 10; break;
		default: break;
		}
		
		return initial;
	}
}
