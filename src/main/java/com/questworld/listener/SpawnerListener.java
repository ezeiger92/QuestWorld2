package com.questworld.listener;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import com.questworld.util.AutoListener;

public class SpawnerListener extends AutoListener {
	private final HashSet<UUID> spawnerEntities = new HashSet<>();

	public SpawnerListener(Plugin plugin) {
		register(plugin);
	}
	
	public boolean isFromSpawner(LivingEntity e) {
		return spawnerEntities.contains(e.getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			spawnerEntities.add(e.getEntity().getUniqueId());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreatureDeath(EntityDeathEvent e) {
		spawnerEntities.remove(e.getEntity().getUniqueId());
	}
}