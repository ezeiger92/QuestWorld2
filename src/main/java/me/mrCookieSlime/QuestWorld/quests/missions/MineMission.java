package me.mrCookieSlime.QuestWorld.quests.missions;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;

public class MineMission extends MissionType implements Listener {
	public MineMission() {
		super("MINE_BLOCK", true, true, false, SubmissionType.BLOCK, new MaterialData(Material.IRON_PICKAXE));
	}
	
	@Override
	public ItemStack getDisplayItem(QuestMission qm) {
		return qm.getMissionItem().clone();
	}
	
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		return "&7Mine " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onMine(BlockBreakEvent e) {
		QuestManager manager = QuestWorld.getInstance().getManager(e.getPlayer());
		for (QuestMission task: QuestManager.block_breaking_tasks) {
			// Check (deprecated D:) block data on block breaks [ezeiger92/QuestWorld2#16]
			if (e.getBlock().getType().equals(task.getMissionItem().getType()) && e.getBlock().getData() == task.getMissionItem().getDurability()) {
				if (manager.getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && !manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
					manager.addProgress(task, 1);
				}
			}
		}
	}
}
