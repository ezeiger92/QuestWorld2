package me.mrCookieSlime.QuestWorld.listeners;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;
import me.mrCookieSlime.QuestWorld.utils.Text;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class TaskListener implements Listener {

	public TaskListener(QuestWorld plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
/*
	// Moved to CraftMission
	@EventHandler
	public void onCraft(final CraftItemEvent e) {
		QuestChecker.check((Player) e.getWhoClicked(), e, "CRAFT", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event) {
				if (QuestWorld.getInstance().isItemSimiliar(e.getRecipe().getResult(), task.getMissionItem())) manager.addProgress(task, e.getCurrentItem().getAmount());
			}
		});
	}
	
	// Moved to FishMission
	@EventHandler
	public void onFish(final PlayerFishEvent e) {
		if (!(e.getCaught() instanceof Item)) return;
		
		QuestChecker.check(e.getPlayer(), e, "FISH", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event) {
				if (QuestWorld.getInstance().isItemSimiliar(((Item) e.getCaught()).getItemStack(), task.getMissionItem())) manager.addProgress(task, ((Item) e.getCaught()).getItemStack().getAmount());
			}
		});
	}
	
	// Moved to LevelMission
	@EventHandler
	public void onXPChange(final PlayerLevelChangeEvent e) {
		QuestChecker.check(e.getPlayer(), e, "REACH_LEVEL", new QuestListener() {
			
			@Override
			public void onProgressCheck(Player p, QuestManager manager, QuestMission task, Object event) {
				manager.setProgress(task, e.getNewLevel());
			}
		});
	}
*/	
	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if (e.getEntity().getLastDamageCause() == null) return;
		Player killer = null;
		
		if (e.getEntity().getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
			if (event.getDamager() instanceof Player) {
				killer = (Player) event.getDamager();
			}
		}
		else if (e.getEntity().getLastDamageCause().getCause().equals(DamageCause.PROJECTILE)) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
			if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
				killer = (Player) ((Projectile) event.getDamager()).getShooter();
			}
		}
		
		if (killer != null) {
			QuestManager manager = QuestWorld.getInstance().getManager(killer);
			for (Category category: QuestWorld.getInstance().getCategories()) {
				for (Quest quest: category.getQuests()) {
					if (category.isWorldEnabled(killer.getWorld().getName())) {
						if (manager.getStatus(quest).equals(QuestStatus.AVAILABLE) && quest.isWorldEnabled(killer.getWorld().getName())) {
							for (QuestMission task: quest.getMissions()) {
								if (!manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
									if (task.getType().getID().equals("KILL") && e.getEntityType().equals(task.getEntity())) {
										if (task.acceptsSpawners()) manager.addProgress(task, 1);
										else if (!e.getEntity().hasMetadata("spawned_by_spawner")) manager.addProgress(task, 1);
									}
									else if (task.getType().getID().equals("KILL_NAMED_MOB") && e.getEntityType().equals(task.getEntity())) {
										String name = e.getEntity() instanceof Player ? ((Player) e.getEntity()).getName(): e.getEntity().getCustomName();
										String entity_name = Text.colorize(task.getEntityName());
										// Succeed if custom name is empty [ezeiger92/QuestWorld2#13]
										if (name != null && (entity_name.equals("") || entity_name.equals(name))) {
											if (task.acceptsSpawners()) manager.addProgress(task, 1);
											else if (!e.getEntity().hasMetadata("spawned_by_spawner")) manager.addProgress(task, 1);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason().equals(SpawnReason.SPAWNER)) e.getEntity().setMetadata("spawned_by_spawner", new FixedMetadataValue(QuestWorld.getInstance(), "QuestWorld"));
	}
}
