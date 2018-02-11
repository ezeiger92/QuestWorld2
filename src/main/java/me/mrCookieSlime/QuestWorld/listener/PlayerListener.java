package me.mrCookieSlime.QuestWorld.listener;

import me.mrCookieSlime.QuestWorld.GuideBook;
import me.mrCookieSlime.QuestWorld.QuestingImpl;
import me.mrCookieSlime.QuestWorld.api.Decaying;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IParty;
import me.mrCookieSlime.QuestWorld.api.contract.IParty.LeaveReason;
import me.mrCookieSlime.QuestWorld.api.contract.IPlayerStatus;
import me.mrCookieSlime.QuestWorld.api.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.api.event.GenericPlayerLeaveEvent;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.manager.ProgressTracker;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onQuestBook(PlayerInteractEvent event) {
		Action a = event.getAction();
		if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)
			if (GuideBook.isGuide(event.getItem()))
				QuestBook.openLastMenu(event.getPlayer());
	}
	
	@EventHandler
	public void onDie(PlayerDeathEvent event) {
		Player p = event.getEntity();
		IPlayerStatus playerStatus = QuestWorld.getPlayerStatus(p);
		
		for(IMission mission : QuestWorld.getViewer().getDecayingMissions())
			if(playerStatus.hasDeathEvent(mission))
				((Decaying) mission.getType()).onDeath(event, QuestWorld.getMissionEntry(mission, p));
	}
	
	HashMap<UUID, Integer> partyKick = new HashMap<>();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		Integer task_id = partyKick.remove(p.getUniqueId());
		if(task_id != null)
			Bukkit.getScheduler().cancelTask(task_id);
		
		if (QuestWorld.getPlugin().getConfig().getBoolean("book.on-first-join") &&
				!ProgressTracker.exists(p.getUniqueId()))
			p.getInventory().addItem(GuideBook.get());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onKick(PlayerKickEvent event) {
		CancellableEvent.send(new GenericPlayerLeaveEvent(event.getPlayer()));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		CancellableEvent.send(new GenericPlayerLeaveEvent(event.getPlayer()));
	}
	
	@EventHandler
	public void onleave(GenericPlayerLeaveEvent event) {
		Player player = event.getPlayer();
		IParty party = QuestWorld.getParty(player);
		
		if(party != null) {
			int autokick = QuestWorld.getPlugin().getConfig().getInt("party.auto-kick", -1);
			
			if(autokick == 0) {
				if(party.isLeader(player))
					QuestWorld.disbandParty(party);
				else
					party.playerLeave(player, LeaveReason.DISCONNECT);
			}
			else if(autokick > 0) {
				int task_id = Bukkit.getScheduler().runTaskLater(QuestWorld.getPlugin(), () -> {
					if(party.isLeader(player))
						QuestWorld.disbandParty(party);
					else
						party.playerLeave(player, LeaveReason.DISCONNECT);
					partyKick.remove(player.getUniqueId());
				}, autokick).getTaskId();
				
				partyKick.put(player.getUniqueId(), task_id);
			}
		}
		
		((QuestingImpl)QuestWorld.getAPI()).unloadPlayerStatus(player);
	}
	
	// Since we can't (yet) randomly update recipes at runtime, replace result with latest lore
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
