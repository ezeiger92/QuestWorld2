package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper.ChatHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.AdvancedMenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.quests.QBDialogue;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.EntityTools;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;

public class MissionButton {

	public static MenuData type(MissionChange changes) {
		
		int totalMissions = QuestWorld.getInstance().getMissionTypes().size();
		String[] missionTypes = new String[totalMissions];
		
		int i = 0;
		int missionIndex = -1;
		
		final String[] keys = QuestWorld.getInstance().getMissionTypes().keySet().toArray(new String[totalMissions]);
		
		for (String type: keys) {
			if(type.equals(changes.getType().toString()))
				missionIndex = i;
			missionTypes[i++] = Text.niceName(type);
		}
		final int currentMission = missionIndex;
		
		ItemStack item = new ItemBuilder(changes.getType().getSelectorItem().toItemStack(1))
				.display("&7" + missionTypes[missionIndex])
				.selector(missionIndex, missionTypes)
				.get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			int delta = clickNumber(0, 1, event);
			if(delta >= 0)
				delta = 1;
			else
				delta = -1;
			
			int newMission = (currentMission + delta + totalMissions) % totalMissions;
			
			changes.setType(QuestWorld.getInstance().getMissionTypes().get(keys[newMission]));
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData item(MissionChange changes) {
		ItemStack item = new ItemBuilder(changes.getMissionItem()).lore(
				"",
				"&e> Click to change the Item to",
				"&ethe Item you are currently holding").get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			Player p = (Player)event.getWhoClicked();
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand != null)
				changes.setItem(hand.clone());
		});
		
		return new MenuData(item, handler);
	}

	public static MenuData amount(MissionChange changes) {
		return amount(changes, 16);
	}
	
	public static MenuData amount(MissionChange changes, int groupSize) {
		ItemStack item = new ItemBuilder(Material.REDSTONE).display("&7Amount: §b" + changes.getAmount()).lore(
				"",
				"&rLeft Click: &e+1",
				"&rRight Click: &e-1",
				"&rShift + Left Click: &e+"+groupSize,
				"&rShift + Right Click: &e-"+groupSize).get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			int amt = clickNumber(changes.getAmount(), groupSize, event);
			changes.setAmount(Math.max(amt, 1));
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData entity(MissionChange changes) {
		EntityType entity = changes.getEntity();
		ItemBuilder egg = new ItemBuilder(EntityTools.getEntityDisplay(entity));
		egg.display("&7Entity Type: &r" + Text.niceName(entity.name()));
		egg.lore("", "&e> Click to change the Entity");
		
		ItemStack item = egg.get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			QBDialogue.openQuestMissionEntityEditor((Player)event.getWhoClicked(), changes.getSource());
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData location(MissionChange changes) {
		ItemStack item = new ItemBuilder(changes.getDisplayItem()).lore(
				"",
				"&e> Click to change the Location",
				"&eto your current Position"
				).get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			changes.setLocation((Player)event.getWhoClicked());
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData entityName(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.NAME_TAG).display("&r" + changes.getEntityName()).lore(
				"",
				"&e> Click to change the Name").get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			Player p = (Player)event.getWhoClicked();
			QuestWorld.getInstance().storeInput(p.getUniqueId(), new Input(InputType.KILL_NAMED, changes.getSource()));
			PlayerTools.sendTranslation(p, true, Translation.killmission_rename);
			p.closeInventory();
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData missionName(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.NAME_TAG).display("").lore(
				changes.getText(),
				"",
				"&rLeft Click: Edit Mission Name",
				"&rRight Click: Reset Mission Name").get();

		MenuClickHandler handler = simpleHandler(changes, event -> {
			Player p = (Player)event.getWhoClicked();
			
			if(event.getClick().isRightClick()) {
				changes.setCustomName(null);
			}
			else {
				p.closeInventory();
				PlayerTools.sendTranslation(p, true, Translation.mission_await);
				MenuHelper.awaitChatInput(p, new ChatHandler() {
					
					@Override
					public boolean onChat(Player p, String message) {
						changes.getSource().setCustomName(message);
						PlayerTools.sendTranslation(p, true, Translation.mission_name);
						QuestBook.openQuestMissionEditor(p, changes.getSource());
						return false;
					}
				});
			}
		});
		
		return new MenuData(item, handler);
	}

	public static MenuData timeframe(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.WATCH).display("&7Complete Mission within: &b" + (changes.getTimeframe() / 60) + "h " + (changes.getTimeframe() % 60) + "m").lore(
				"",
				"&rLeft Click: &e+1m",
				"&rRight Click: &e-1m",
				"&rShift + Left Click: &e+1h",
				"&rShift + Right Click: &e-1h").get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			int amt = clickNumber((int)changes.getTimeframe(), 60, event);
			changes.setTimeframe(Math.max(amt, 0));
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData deathReset(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.SKULL_ITEM).display("&7Resets on Death: " + (changes.resetsonDeath() ? "&2&l\u2714": "&4&l\u2718")).lore(
				"",
				"&e> Click to change whether this Mission's Progress",
				"&eresets when a Player dies").get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			changes.setDeathReset(!changes.resetsonDeath());
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData customInt(MissionChange changes, int groupSize) {
		ItemStack item = new ItemBuilder(Material.BARRIER).display("Custom Int = " + changes.getCustomInt()).get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			int amt = clickNumber(changes.getCustomInt(), groupSize, event);
			changes.setCustomInt(amt);
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData spawnersAllowed(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.MOB_SPAWNER).display("&7Allow Mobs from Spawners: " + (changes.acceptsSpawners() ? "&2&l\u2714": "&4&l\u2718")).lore(
				"",
				"&e> Click to change whether this Mission will",
				"&ealso count Mobs which were spawned by a Mob Spawner").get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			changes.setSpawnerSupport(!changes.acceptsSpawners());
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuData dialogue(MissionChange changes) {
		ItemStack item = new ItemBuilder(Material.PAPER).display("&rDialogue").lore(
				"",
				"&rLeft Click: Edit the Dialogue",
				"&rRight Click: Dialogue Preview").get();
		
		MenuClickHandler handler = simpleHandler(changes, event -> {
			Player p = (Player)event.getWhoClicked();
			if (event.getClick().isRightClick()) {
				p.closeInventory();
				if (changes.getSource().getDialogue().isEmpty()) p.sendMessage("§4No Dialogue found!");
				else QuestWorld.getInstance().getManager(p).sendQuestDialogue(p, changes.getSource(), changes.getSource().getDialogue().iterator());
			}
			else {
				p.closeInventory();
				changes.setupDialogue(p);
			}
		});
		
		return new MenuData(item, handler);
	}
	
	public static MenuClickHandler simpleHandler(MissionChange changes, Consumer<InventoryClickEvent> action) {
		return new AdvancedMenuClickHandler() {
			// Unused in advanced click
			@Override
			public boolean onClick(Player p, int arg1, ItemStack arg2, ClickAction arg3) {
				return true;
			}

			@Override
			public boolean onClick(InventoryClickEvent event, Player p, int arg2, ItemStack arg3, ClickAction arg4) {
				action.accept(event);
				if(changes.sendEvent()) {
					changes.apply();
					QuestBook.openQuestMissionEditor(p, changes.getSource());
				}
				
				return false;
			}
			
		};
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
