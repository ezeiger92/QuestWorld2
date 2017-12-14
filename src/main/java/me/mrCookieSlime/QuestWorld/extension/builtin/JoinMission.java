package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;

public class JoinMission extends MissionType implements Listener {
	public JoinMission() {
		super("JOIN", true, new ItemStack(Material.GOLD_NUGGET));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemStack(Material.WATCH);
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Join " + instance.getAmount() + " times";
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		for(MissionEntry r : QuestWorld.getMissionEntries(this, e.getPlayer()))
			r.addProgress(1);
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(17, MissionButton.amount(changes));
	}
}
