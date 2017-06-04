package me.mrCookieSlime.QuestWorld.quests.pluginmissions;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.wasteofplastic.askyblock.events.IslandLevelEvent;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.quests.QuestChecker;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.QuestOfflineListener;

public class ASkyBlockLevelMission extends MissionType implements Listener {
	public ASkyBlockLevelMission() {
		super("ASKYBLOCK_REACH_ISLAND_LEVEL", false, false, false, SubmissionType.INTEGER, new MaterialData(Material.GRASS));
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
		QuestChecker.check(e.getPlayer(), e, "ASKYBLOCK_REACH_ISLAND_LEVEL", new QuestOfflineListener() {
			
			@Override
			public void onProgressCheck(UUID uuid, QuestManager manager, Mission task, Object event) {
				manager.setProgress(task, e.getLevel());
			}
		});
	}
}
