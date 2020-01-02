package com.questworld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;

public class JoinMission extends MissionType implements Listener {
	public JoinMission() {
		super("JOIN", true, new ItemStack(Material.GOLD_NUGGET));
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemStack(Material.CLOCK);
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Join " + instance.getAmount() + " times";
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		for (MissionEntry r : QuestWorld.getMissionEntries(this, e.getPlayer()))
			r.addProgress(1);
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(17, MissionButton.amount(changes));
	}
}
