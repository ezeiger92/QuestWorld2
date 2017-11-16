package me.mrCookieSlime.QuestWorld.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.QuestingAPI;

public class TaskListener implements Listener {

	public TaskListener(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason().equals(SpawnReason.SPAWNER))
			e.getEntity().setMetadata("spawned_by_spawner", new FixedMetadataValue(QuestingAPI.getPlugin(), "QuestWorld"));
	}
}