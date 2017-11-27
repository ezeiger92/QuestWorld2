package me.mrCookieSlime.QuestWorld.listener;

import me.mrCookieSlime.QuestWorld.GuideBook;
import me.mrCookieSlime.QuestWorld.api.Decaying;
import me.mrCookieSlime.QuestWorld.api.MissionSet;
import me.mrCookieSlime.QuestWorld.api.QuestStatus;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.manager.ProgressTracker;
import me.mrCookieSlime.QuestWorld.party.Party;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onQuestBook2(PlayerInteractEvent event) {
		Action a = event.getAction();
		if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)
			if (GuideBook.isGuide(event.getItem()))
				QuestBook.openLastMenu(event.getPlayer());
	}
	
	@EventHandler
	public void onDie(PlayerDeathEvent event) {
		Player p = event.getEntity();
		PlayerManager manager = PlayerManager.of(p);
		String worldName = p.getWorld().getName();
		
		for(IMission task : QuestWorld.getViewer().getDecayingMissions()) {
			IQuest quest = task.getQuest();
			if (!manager.getStatus(quest).equals(QuestStatus.AVAILABLE)
					|| !quest.getWorldEnabled(worldName)
					|| !quest.getCategory().isWorldEnabled(worldName)
					|| !task.getDeathReset())
				continue;

			((Decaying) task).onDeath(event, new MissionSet.Result(task, manager));
		}
	}
	
	HashMap<UUID, Integer> partyKick = new HashMap<>();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		int task_id = partyKick.getOrDefault(p.getUniqueId(), -1);
		if(task_id > -1) {
			Bukkit.getScheduler().cancelTask(task_id);
			partyKick.remove(task_id);
		}
		
		if (QuestWorld.getPlugin().getConfig().getBoolean("book.on-first-join") &&
				!ProgressTracker.exists(p.getUniqueId()))
			p.getInventory().addItem(GuideBook.get());
	}
	
	@EventHandler
	public void onleave(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		
		int autokick = QuestWorld.getPlugin().getConfig().getInt("party.autokick", 2);
		if(autokick == 0) {
			Party party = PlayerManager.of(player).getParty();
			if(party.isLeader(player))
				party.abandon();
			else
				party.kickPlayer(player);
		}
		else if(autokick > 0) {
			Party party = PlayerManager.of(player).getParty();
			int task_id = new BukkitRunnable(){
				@Override
				public void run() {
					if(party.isLeader(player))
						party.abandon();
					else
						party.kickPlayer(player);
					partyKick.remove(getTaskId());
				}
			}.runTaskLater(QuestWorld.getPlugin(), autokick).getTaskId();
			
			partyKick.put(player.getUniqueId(), task_id);
		}

		PlayerManager.of(player).unload();
	}
	
	// Since we can't randomly update recipes at runtime, replace result with latest lore
	@EventHandler
	public void preCraft(PrepareItemCraftEvent e) {
		boolean hasTable = false;
		for(ItemStack is : e.getInventory().getMatrix())
			if(is != null) {
				if(is.getType() == Material.WORKBENCH && !hasTable)
					hasTable = true;
				else
					return;
			}
		
		if(hasTable)
			e.getInventory().setResult(GuideBook.get());
	}
}
