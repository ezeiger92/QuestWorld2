package com.questworld.extension.builtin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.Decaying;
import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MenuData;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.ItemBuilder;
import com.questworld.util.PlayerTools;
import com.questworld.util.Text;

public class MineMission extends MissionType implements Listener, Decaying {
	public MineMission() {
		super("MINE_BLOCK", true);
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getItem();
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Mine " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMine(BlockBreakEvent e) {
		for (MissionEntry r : QuestWorld.getMissionEntries(this, e.getPlayer())) {
			if (ItemBuilder.compareItems(PlayerTools.getStackOf(e.getBlock()), r.getMission().getItem()))
				r.addProgress(1);
		}
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, new MenuData(
				new ItemBuilder(changes.getDisplayItem()).wrapLore(
						"",
						"&e> Click to set the block type").get(),
				event -> {
					ItemStack mainItem = PlayerTools.getMainHandItem(event.getWhoClicked());
					if (mainItem != null && mainItem.getType().isBlock()) {
						mainItem = mainItem.clone();
						mainItem.setAmount(1);
						changes.setItem(mainItem);
					}
					MissionButton.apply(event, changes);
				}
		));
		
		putButton(17, MissionButton.amount(changes));
	}
}
