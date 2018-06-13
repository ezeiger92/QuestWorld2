package com.questworld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.Ticking;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MenuData;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.ItemBuilder;
import com.questworld.util.Text;

public class PlayMission extends MissionType implements Ticking {
	public PlayMission() {
		super("PLAY_TIME", false, new ItemStack(Material.WATCH));
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return getSelectorItem().clone();
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Play for " + Text.timeFromNum(instance.getAmount());
	}

	@Override
	public String progressString(int current, int total) {
		float percent = current / (float) total;
		int remaining = total - current;
		return Math.round(percent * 100) + "% (" + Text.timeFromNum(remaining) + " remaining)";
	}

	@Override
	public void onManual(Player player, MissionEntry result) {
		result.setProgress(player.getStatistic(Statistic.PLAY_ONE_TICK) / 20 / 60);
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(17,
				new MenuData(new ItemBuilder(Material.WATCH)
						.wrapText("&7Time: &b" + Text.timeFromNum(changes.getAmount()), "", "&rLeft click: &e+1m",
								"&rRight click: &e-1m", "&rShift left click: &e+1h", "&rShift right click: &e-1h")
						.get(), event -> {
							int amount = MissionButton.clickNumber(changes.getAmount(), 60, event);
							if (amount < 1)
								amount = 1;
							changes.setAmount(amount);
							MissionButton.apply(event, changes);
						}));
	}
}
