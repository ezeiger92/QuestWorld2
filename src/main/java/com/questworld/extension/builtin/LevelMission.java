package com.questworld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.Ticking;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;

public class LevelMission extends MissionType implements Listener, Ticking {
	public LevelMission() {
		super("REACH_LEVEL", false, new ItemStack(Material.EXPERIENCE_BOTTLE));
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemStack(Material.COMMAND_BLOCK);
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Reach Level " + instance.getAmount();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onXPChange(final PlayerLevelChangeEvent e) {
		for (MissionEntry r : QuestWorld.getMissionEntries(this, e.getPlayer()))
			r.setProgress(e.getNewLevel());
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(17, MissionButton.amount(changes));
	}

	@Override
	public void onManual(Player player, MissionEntry entry) {
		entry.setProgress(player.getLevel());
	}
}
