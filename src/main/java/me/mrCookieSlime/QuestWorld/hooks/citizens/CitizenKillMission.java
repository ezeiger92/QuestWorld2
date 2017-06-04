package me.mrCookieSlime.QuestWorld.hooks.citizens;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;

public class CitizenKillMission extends MissionType implements Listener {
	public CitizenKillMission() {
		super("KILL_NPC", true, true, false, SubmissionType.CITIZENS_KILL,
				new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get().getData());
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get();
	}
	
	@Override
	protected String displayString(IMission instance) {
		String name = "N/A";
		NPC npc = CitizensHook.npcFrom(instance);
		if(npc != null)
			name = npc.getName();
		String times = "";
		if(instance.getAmount() > 1)
			times = " " + instance.getAmount() + " times";
		
		return "&7Kill " + name + times;
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
						for (Mission task: quest.getMissions()) {
							if (!manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
								if (task.getType().getID().equals("KILL_NPC") && e.getNPC().getId() == task.getCustomInt()) {
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
