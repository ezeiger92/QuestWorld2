package me.mrCookieSlime.QuestWorld.listener;

import me.mrCookieSlime.QuestWorld.GuideBook;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Decaying;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.LazyMetadataValue;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onQuestBook2(PlayerInteractEvent event) {
		Action a = event.getAction();
		if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)
			if (GuideBook.get().isSimilar(event.getItem()))
				QuestBook.openLastMenu(event.getPlayer());
	}
	
	@EventHandler
	public void onDie(PlayerDeathEvent event) {
		Player p = event.getEntity();
		PlayerManager manager = PlayerManager.of(p);
		String worldName = p.getWorld().getName();
		
		for(IMission task : QuestWorld.get().getDecayingMissions()) {
			IQuest quest = task.getQuest();
			if (!manager.getStatus(quest).equals(QuestStatus.AVAILABLE)
					|| !quest.isWorldEnabled(worldName)
					|| !quest.getCategory().isWorldEnabled(worldName))
				continue;
			
			int amount = ((Decaying) task).onDeath(event, task);
			if(amount >= 0)
				manager.setProgress(task, amount);
			else
				manager.addProgress(task, amount);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		final UUID uuid = e.getPlayer().getUniqueId();
		e.getPlayer().setMetadata("questworld.playermanager", new LazyMetadataValue(QuestWorld.get(), () ->
			new PlayerManager(Bukkit.getPlayer(uuid))
		));
		
		if (QuestWorld.get().getConfig().getBoolean("book.on-first-join") &&
				!PlayerManager.of(e.getPlayer()).getTracker().exists())
			e.getPlayer().getInventory().addItem(GuideBook.get());
	}
	
	@EventHandler
	public void onleave(PlayerQuitEvent e) {
		PlayerManager.of(e.getPlayer()).unload();
	}
}
