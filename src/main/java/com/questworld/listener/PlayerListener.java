package com.questworld.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.GuideBook;
import com.questworld.QuestingImpl;
import com.questworld.api.Decaying;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IParty;
import com.questworld.api.contract.IParty.LeaveReason;
import com.questworld.api.contract.IPlayerStatus;
import com.questworld.api.event.CancellableEvent;
import com.questworld.api.event.GenericPlayerLeaveEvent;
import com.questworld.api.menu.QuestBook;
import com.questworld.manager.ProgressTracker;
import com.questworld.util.AutoListener;
import com.questworld.util.version.ObjectMap.VDMaterial;

public class PlayerListener extends AutoListener {
	private QuestingImpl api;

	public PlayerListener(QuestingImpl api) {
		this.api = api;
		register(api.getPlugin());
	}

	@EventHandler
	public void onQuestBook(PlayerInteractEvent event) {
		Action a = event.getAction();
		if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)
			if (GuideBook.isGuide(event.getItem()))
				QuestBook.openLastMenu(event.getPlayer());
	}

	@EventHandler
	public void onDie(PlayerDeathEvent event) {
		Player p = event.getEntity();
		IPlayerStatus playerStatus = api.getPlayerStatus(p);

		for (IMission mission : api.getViewer().getDecayingMissions())
			if (playerStatus.hasDeathEvent(mission))
				((Decaying) mission.getType()).onDeath(event, api.getMissionEntry(mission, p));
	}

	HashMap<UUID, Integer> partyKick = new HashMap<>();

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		Integer task_id = partyKick.remove(p.getUniqueId());
		if (task_id != null)
			Bukkit.getScheduler().cancelTask(task_id);

		if (api.getPlugin().getConfig().getBoolean("book.on-first-join") && !ProgressTracker.exists(p.getUniqueId()))
			p.getInventory().addItem(GuideBook.instance().item());
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
	public void onLeave(GenericPlayerLeaveEvent event) {
		Player player = event.getPlayer();
		IParty party = api.getParty(player);

		if (party != null) {
			int autokick = api.getPlugin().getConfig().getInt("party.auto-kick", -1);

			if (autokick == 0) {
				if (party.isLeader(player))
					api.disbandParty(party);
				else
					party.playerLeave(player, LeaveReason.DISCONNECT);
			}
			else if (autokick > 0) {
				int task_id = Bukkit.getScheduler().runTaskLater(api.getPlugin(), () -> {
					if (party.isLeader(player))
						api.disbandParty(party);
					else
						party.playerLeave(player, LeaveReason.DISCONNECT);

					partyKick.remove(player.getUniqueId());
				}, autokick).getTaskId();

				partyKick.put(player.getUniqueId(), task_id);
			}
		}

		api.unloadPlayerStatus(player);
	}

	// Since we can't (yet) randomly update recipes at runtime, replace result with
	// latest lore
	@EventHandler
	public void preCraft(PrepareItemCraftEvent e) {
		boolean hasTable = false;
		for (ItemStack is : e.getInventory().getMatrix())
			if (is != null) {
				if (is.getType() == VDMaterial.CRAFTING_TABLE && !hasTable)
					hasTable = true;
				else
					return;
			}

		if (hasTable)
			e.getInventory().setResult(GuideBook.instance().item());
	}
}
