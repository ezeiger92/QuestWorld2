package me.mrCookieSlime.QuestWorld.extensions.askyblock;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.wasteofplastic.askyblock.events.IslandLevelEvent;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class ASkyBlockLevelMission extends MissionType implements Listener {
	public ASkyBlockLevelMission() {
		super("ASKYBLOCK_REACH_ISLAND_LEVEL", false, false, new ItemStack(Material.GRASS));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemBuilder(Material.COMMAND).display("&7" + instance.getAmount()).get();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Reach Island Level " + instance.getAmount();
	}
	
	@EventHandler
	public void onWin(final IslandLevelEvent e) {
		QuestWorld.getInstance().getManager(e.getPlayer().toString()).forEachTaskOf(this, mission -> true, e.getLevel(), true);
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(17, MissionButton.amount(changes));
	}
}
