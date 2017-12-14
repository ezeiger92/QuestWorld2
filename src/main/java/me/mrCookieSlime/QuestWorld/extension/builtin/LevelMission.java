package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;

public class LevelMission extends MissionType implements Listener {
	public LevelMission() {
		super("REACH_LEVEL", false, new ItemStack(Material.EXP_BOTTLE));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemStack(Material.COMMAND);
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Reach Level " + instance.getAmount();
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
	public void onXPChange(final PlayerLevelChangeEvent e) {
		for(MissionEntry r : QuestWorld.getMissionEntries(this, e.getPlayer()))
			r.setProgress(e.getNewLevel());
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(17, MissionButton.amount(changes));
	}
}
