package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

public class PlayMission extends MissionType implements Ticking {
	public PlayMission() {
		super("PLAY_TIME", false, false, new ItemStack(Material.WATCH));
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
	public String progressString(float percent, int current, int total) {
		int remaining = total - current;
		return Math.round(percent * 100) + "% (" + Text.timeFromNum(remaining) + " remaining)";
	}
	
	@Override
	public int onManual(Player player, IMission mission) {
		return player.getStatistic(Statistic.PLAY_ONE_TICK) / 20 / 60;
	}
	
	@Override
	protected void layoutMenu(IMissionWrite changes) {
		super.layoutMenu(changes);
		putButton(17, new MenuData(
				new ItemBuilder(Material.WATCH).display("&7Time: &b" + Text.timeFromNum(changes.getAmount())).lore(
						"",
						"&rLeft Click: &e+1m",
						"&rRight Click: &e-1m",
						"&rShift + Left Click: &e+1h",
						"&rShift + Right Click: &e-1h"
						).get(),
				 event -> {
					int amount = MissionButton.clickNumber(changes.getAmount(), 60, event);
					if(amount < 1)
						amount = 1;
					changes.setAmount(amount);
					MissionButton.apply(event, changes);
				}
		));
	}
}
