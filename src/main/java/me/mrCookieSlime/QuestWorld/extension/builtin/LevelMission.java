package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;

public class LevelMission extends MissionType implements Listener {
	public LevelMission() {
		super("REACH_LEVEL", false, new ItemStack(Material.EXP_BOTTLE));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemBuilder(Material.COMMAND).display("&7" + instance.getAmount()).get();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Reach Level " + instance.getAmount();
	}
	
	@EventHandler
	public void onXPChange(final PlayerLevelChangeEvent e) {
		QuestWorld.getInstance().getManager(e.getPlayer()).forEachTaskOf(this, mission -> true);
	}
	
	@Override
	protected void layoutMenu(IMissionWrite changes) {
		super.layoutMenu(changes);
		putButton(17, MissionButton.amount(changes));
	}
}
