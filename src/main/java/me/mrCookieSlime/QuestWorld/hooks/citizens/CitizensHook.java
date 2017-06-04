package me.mrCookieSlime.QuestWorld.hooks.citizens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.mrCookieSlime.CSCoreLibPlugin.PlayerRunnable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.CustomBookOverlay;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Particles.MC_1_8.ParticleEffect;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerInventory;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class CitizensHook extends QuestExtension implements Listener {
	public static Map<UUID, Mission> link = new HashMap<UUID, Mission>();
	
	public static NPC npcFrom(IMission instance) {
		return npcFrom(instance.getCustomInt());
	}
	
	private MissionType[] missions;
	
	public static NPC npcFrom(int id) {
		return CitizensAPI.getNPCRegistry().getById(id);
	}
	
	@Override
	public String[] getDepends() {
		return new String[] { "Citizens" };
	}
	
	@Override
	public void initialize(Plugin parent) {
		missions = new MissionType[] {
			new CitizenInteractMission(),
			new CitizenSubmitMission(),
			new CitizenKillMission(),
			new CitizenAcceptQuestMission(),
		};
		
		parent.getServer().getPluginManager().registerEvents(this, parent);
		
		parent.getServer().getScheduler().scheduleSyncRepeatingTask(parent, new Runnable() {
			
			@Override
			public void run() {
				for (Mission task: QuestManager.getCitizenTasks()) {
					NPC npc = npcFrom(task);
					if (npc != null && npc.getEntity() != null) {
						List<Player> players = new ArrayList<Player>();
						
						for (Entity n: npc.getEntity().getNearbyEntities(20D, 8D, 20D)) {
							if (n instanceof Player) {
								QuestManager manager = QuestWorld.getInstance().getManager((Player) n);
								if (manager.getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && manager.hasUnlockedTask(task) && !manager.hasCompletedTask(task)) {
									players.add((Player) n);
								}
							}
						}
						if (!players.isEmpty()) {
							try {
								ParticleEffect.VILLAGER_HAPPY.display(npc.getEntity().getLocation().add(0, 1, 0), 0.5F, 0.7F, 0.5F, 0, 20, players);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}, 0L, 12L);
	}
	
	@Override
	public MissionType[] getMissions() {
		return missions;
	}

	//TODO "for for if for if if" KILL IT WITH FIRE
	@EventHandler
	public void onInteract(NPCRightClickEvent e) {
		Player p = e.getClicker();
		if (link.containsKey(p.getUniqueId())) {
			link.get(p.getUniqueId()).setCustomInt(e.getNPC().getId());
			QuestBook.openQuestMissionEditor(p, link.get(p.getUniqueId()));
			link.remove(p.getUniqueId());
			PlayerTools.sendTranslation(p, true, Translation.citizen_link);
		}
		else {
			final QuestManager manager = QuestWorld.getInstance().getManager(p);
			for (Category category: QuestWorld.getInstance().getCategories()) {
				for (Quest quest: category.getQuests()) {
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						for (final Mission task: quest.getMissions()) {
							if (!manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
								if (task.getType().getID().equals("CITIZENS_INTERACT") && e.getNPC().getId() == task.getCustomInt()) {
									manager.addProgress(task, task.getAmount());
								}
								else if (task.getType().getID().equals("ACCEPT_QUEST_FROM_NPC") && e.getNPC().getId() == task.getCustomInt()) {
									TellRawMessage lore = new TellRawMessage();
									lore.addText(e.getNPC().getName() + ":\n\n");
									lore.addText(task.getLore());
									lore.color(ChatColor.DARK_AQUA);
									lore.addText("\n\n    ");
									lore.addText(Text.colorize("&7( &a&l\u2714 &7)"));
									lore.addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to accept this Quest"));
									lore.addClickEvent(new PlayerRunnable(3) {
										
										@Override
										public void run(Player p) {
											manager.addProgress(task, task.getAmount());
										}
									});
									lore.addText("      ");
									lore.addText(Text.colorize("&7( &4&l\u2718 &7)"));
									lore.addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to do this Quest later"));
									lore.addClickEvent(new PlayerRunnable(3) {
										
										@Override
										public void run(Player p) {
										}
									});
									new CustomBookOverlay("Quest", "TheBusyBiscuit", lore).open(p);
								}
								else if (task.getType().getID().equals("CITIZENS_SUBMIT") && e.getNPC().getId() == task.getCustomInt() && QuestWorld.getInstance().isItemSimiliar(PlayerTools.getActiveHandItem(p), task.getMissionItem())) {
									int rest = QuestWorld.getInstance().getManager(p).addProgress(task, PlayerTools.getActiveHandItem(p).getAmount());
									if (rest > 0) PlayerTools.setActiveHandItem(p, new CustomItem(PlayerTools.getActiveHandItem(p), rest));
									else PlayerTools.setActiveHandItem(p, null);
									
									PlayerInventory.update(p);
								}
							}
						}
					}
				}
			}
		}
	}
}
