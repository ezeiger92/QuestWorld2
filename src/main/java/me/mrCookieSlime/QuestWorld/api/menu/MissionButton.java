package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.EntityTools;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;

public class MissionButton {
	public static MenuData item(MissionChange changes) {
		ItemStack item = new ItemBuilder(changes.getMissionItem()).lore(
			"",
			"&e> Click to change the Item to",
			"&ethe Item you are currently holding").get();

		return new MenuData(item, event -> {
			Player p = (Player)event.getWhoClicked();
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand != null && hand.getType() != Material.AIR)
				changes.setItem(hand.clone());
			apply(event, changes);
		});
	}

	public static MenuData amount(MissionChange changes) {
		return amount(changes, 16);
	}
	
	public static MenuData amount(MissionChange changes, int groupSize) {
		ItemStack item = new ItemBuilder(Material.REDSTONE).display("&7Amount: &b" + changes.getAmount()).lore(
			"",
			"&rLeft Click: &e+1",
			"&rRight Click: &e-1",
			"&rShift + Left Click: &e+"+groupSize,
			"&rShift + Right Click: &e-"+groupSize).get();

		return new MenuData(item, event -> {
			int amt = clickNumber(changes.getAmount(), groupSize, event);
			changes.setAmount(Math.max(amt, 1));
			apply(event, changes);
		});
	}
	
	public static MenuData entity(MissionChange changes) {
		EntityType entity = changes.getEntity();
		ItemStack item = new ItemBuilder(EntityTools.getEntityDisplay(entity))
			.display("&7Entity Type: &r" + Text.niceName(entity.name())).lore(
				"",
				"&e> Click to change the Entity").get();

		return new MenuData(item, event -> {
			QBDialogue.openQuestMissionEntityEditor((Player)event.getWhoClicked(), changes.getSource());
			apply(event, changes);
		});
	}
	
	public static MenuData location(MissionChange changes) {
		ItemStack item = new ItemBuilder(changes.getDisplayItem()).lore(
			"",
			"&e> Click to change the Location",
			"&eto your current Position").get();

		return new MenuData(item, event -> {
			changes.setLocation((Player)event.getWhoClicked());
			apply(event, changes);
		});
	}
	
	public static MenuData entityName(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.NAME_TAG).display("&r" + changes.getCustomString()).lore(
			"",
			"&e> Click to change the Name").get();
		
		return new MenuData(item, event -> {
			Player p = (Player)event.getWhoClicked();
			PlayerTools.promptInput(p, new SinglePrompt(
					PlayerTools.makeTranslation(true, Translation.killmission_rename),
					(c,s) -> {
						changes.setCustomString(Text.colorize(s));
						if(changes.sendEvent()) {
							changes.apply();
							PlayerTools.sendTranslation(p, true, Translation.killtype_rename);
						}
						QuestBook.openQuestMissionEditor(p, changes.getSource());
						return true;
					}
			));

			PlayerTools.closeInventoryWithEvent(p);
		});
	}
	
	public static MenuData missionName(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.NAME_TAG).display("").lore(
			changes.getText(),
			"",
			"&rLeft Click: Edit Mission Name",
			"&rRight Click: Reset Mission Name").get();

		return new MenuData(item, event -> {
			Player p = (Player)event.getWhoClicked();
			
			if(event.getClick().isRightClick()) {
				changes.setDisplayName(null);
				apply(event, changes);
			}
			else {
				PlayerTools.closeInventoryWithEvent(p);
				PlayerTools.promptInput(p, new SinglePrompt(
						PlayerTools.makeTranslation(true, Translation.mission_await),
						(c,s) -> {
							changes.setDisplayName(s);
							if(changes.sendEvent()) {
								PlayerTools.sendTranslation(p, true, Translation.mission_name);
								changes.apply();
								QuestBook.openQuestMissionEditor(p, changes.getSource());
							}
							return true;
						}
				));
			}
		});
	}

	public static MenuData timeframe(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.WATCH).display("&7Complete Mission within: &b" + (changes.getTimeframe() / 60) + "h " + (changes.getTimeframe() % 60) + "m").lore(
			"",
			"&rLeft Click: &e+1m",
			"&rRight Click: &e-1m",
			"&rShift + Left Click: &e+1h",
			"&rShift + Right Click: &e-1h").get();

		return new MenuData(item, event -> {
			int amt = clickNumber(changes.getTimeframe(), 60, event);
			changes.setTimeframe(Math.max(amt, 0));
			apply(event, changes);
		});
	}
	
	public static MenuData deathReset(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.SKULL_ITEM).display("&7Resets on Death: " + (changes.resetsonDeath() ? "&2&l\u2714": "&4&l\u2718")).lore(
			"",
			"&e> Click to change whether this Mission's Progress",
			"&eresets when a Player dies").get();

		return new MenuData(item, event -> {
			changes.setDeathReset(!changes.resetsonDeath());
			apply(event, changes);
		});
	}
	
	public static MenuData customInt(MissionChange changes, int groupSize) {
		ItemStack item = new ItemBuilder(Material.BARRIER).display("Custom Int = " + changes.getCustomInt()).get();

		return new MenuData(item, event -> {
			int amt = clickNumber(changes.getCustomInt(), groupSize, event);
			changes.setCustomInt(amt);
			apply(event, changes);
		});
	}
	
	public static MenuData spawnersAllowed(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.MOB_SPAWNER).display("&7Allow Mobs from Spawners: " + (changes.acceptsSpawners() ? "&2&l\u2714": "&4&l\u2718")).lore(
			"",
			"&e> Click to change whether this Mission will",
			"&ealso count Mobs which were spawned by a Mob Spawner").get();
		
		return new MenuData(item, event -> {
			changes.setSpawnerSupport(!changes.acceptsSpawners());
			apply(event, changes);
		});
	}
	
	public static MenuData dialogue(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.PAPER).display("&rDialogue").lore(
			"",
			"&rLeft Click: Edit the Dialogue",
			"&rRight Click: Dialogue Preview").get();

		return new MenuData(item, event -> {
			Player p = (Player)event.getWhoClicked();
			if (event.getClick().isRightClick()) {
				PlayerTools.closeInventoryWithEvent(p);
				if (changes.getSource().getDialogue().isEmpty())
					p.sendMessage(Text.colorize("&4No Dialogue found!"));
				else QuestWorld.getInstance().getManager(p).sendQuestDialogue(p, changes.getSource(), changes.getSource().getDialogue().iterator());
			}
			else {
				PlayerTools.closeInventoryWithEvent(p);
				changes.getSource().setupDialogue(p);
			}
		});
	}
	
	public static void apply(InventoryClickEvent event, MissionChange changes) {
		if(changes.sendEvent()) {
			changes.apply();
			QuestBook.openQuestMissionEditor((Player) event.getWhoClicked(), changes.getSource());
		}
	}

	public static MenuData simpleButton(MissionChange changes, ItemStack item, Consumer<InventoryClickEvent> action)  {
		return new MenuData(item, action.andThen(event -> apply(event, changes)));
	}
	
	public static int clickNumber(int initial, int groupSize, InventoryClickEvent event) {
		ClickType click = event.getClick();
		switch(click) {
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
