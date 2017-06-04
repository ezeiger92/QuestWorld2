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
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;

public class MineMission extends MissionType implements Listener {
	public MineMission() {
		super("MINE_BLOCK", true, true, false, SubmissionType.BLOCK, new MaterialData(Material.IRON_PICKAXE));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Mine " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onMine(BlockBreakEvent e) {
		QuestManager manager = QuestWorld.getInstance().getManager(e.getPlayer());
		for (Mission task: QuestManager.block_breaking_tasks) {
			ItemStack is = PlayerTools.getStackOf(e.getBlock());
			if (is.isSimilar(task.getMissionItem())) {
				if (manager.getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && !manager.hasCompletedTask(task) && manager.hasUnlockedTask(task)) {
					manager.addProgress(task, 1);
				}
			}
		}
	}
}
