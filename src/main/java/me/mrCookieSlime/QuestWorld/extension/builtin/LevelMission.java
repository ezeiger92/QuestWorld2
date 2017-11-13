package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionSet;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
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
	
	@EventHandler
	public void onXPChange(final PlayerLevelChangeEvent e) {
		for(MissionSet.Result r : MissionSet.of(this, e.getPlayer()))
			r.addProgress(1);
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		super.layoutMenu(changes);
		putButton(17, MissionButton.amount(changes));
	}
}
