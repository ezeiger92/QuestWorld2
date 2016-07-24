package me.mrCookieSlime.QuestWorld.hooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.mrCookieSlime.CSCoreLibPlugin.PlayerRunnable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.CustomBookOverlay;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerInventory;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class CitizensListener implements Listener {

	public CitizensListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public static Map<UUID, QuestMission> link = new HashMap<UUID, QuestMission>();
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(NPCRightClickEvent e) {
		Player p = e.getClicker();
		if (link.containsKey(p.getUniqueId())) {
			link.get(p.getUniqueId()).setCitizen(e.getNPC().getId());
			QuestBook.openQuestMissionEditor(p, link.get(p.getUniqueId()));
			link.remove(p.getUniqueId());
			QuestWorld.getInstance().getLocalization().sendTranslation(p, "editor.link-citizen-finished", true);
		}
		else {
			final QuestManager manager = QuestWorld.getInstance().getManager(p);
			for (Category category: QuestWorld.getInstance().getCategories()) {
				for (Quest quest: category.getQuests()) {
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(p.getWorld().getName())) {
						for (final QuestMission task: quest.getMissions()) {
							if (!manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
								if (task.getType().getID().equals("CITIZENS_INTERACT") && e.getNPC().getId() == task.getCitizenID()) {
									manager.addProgress(task, task.getAmount());
								}
								else if (task.getType().getID().equals("ACCEPT_QUEST_FROM_NPC") && e.getNPC().getId() == task.getCitizenID()) {
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
								else if (task.getType().getID().equals("CITIZENS_SUBMIT") && e.getNPC().getId() == task.getCitizenID() && QuestWorld.getInstance().isItemSimiliar(p.getInventory().getItemInHand(), task.getItem())) {
									int rest = QuestWorld.getInstance().getManager(p).addProgress(task, p.getInventory().getItemInHand().getAmount());
									if (rest > 0) p.getInventory().setItemInHand(new CustomItem(p.getInventory().getItemInHand(), rest));
									else p.getInventory().setItemInHand(null);
									
									PlayerInventory.update(p);
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInteract(NPCDeathEvent e) {
		if (e.getNPC().getEntity().getLastDamageCause() == null) return;
		Player killer = null;
		
		if (e.getNPC().getEntity().getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e.getNPC().getEntity().getLastDamageCause();
			if (event.getDamager() instanceof Player) {
				killer = (Player) event.getDamager();
			}
		}
		else if (e.getNPC().getEntity().getLastDamageCause().getCause().equals(DamageCause.PROJECTILE)) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e.getNPC().getEntity().getLastDamageCause();
			if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
				killer = (Player) ((Projectile) event.getDamager()).getShooter();
			}
		}
		
		if (killer != null) {
			QuestManager manager = QuestWorld.getInstance().getManager(killer);
			for (Category category: QuestWorld.getInstance().getCategories()) {
				for (Quest quest: category.getQuests()) {
					if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(killer.getWorld().getName())) {
						for (QuestMission task: quest.getMissions()) {
							if (!manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
								if (task.getType().getID().equals("KILL_NPC") && e.getNPC().getId() == task.getCitizenID()) {
									manager.addProgress(task, 1);
								}
							}
						}
					}
				}
			}
		}
	}
	
}
