package me.mrCookieSlime.QuestWorld.hooks.askyblock;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.wasteofplastic.askyblock.events.IslandLevelEvent;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;

public class ASkyBlockLevelMission extends MissionType implements Listener {
	public ASkyBlockLevelMission() {
		super("ASKYBLOCK_REACH_ISLAND_LEVEL", false, false, new MaterialData(Material.GRASS));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return new CustomItem(Material.COMMAND, "&7" + instance.getAmount(), 0);
	}
	
	@Override
	protected String displayString(IMission instance) {
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
