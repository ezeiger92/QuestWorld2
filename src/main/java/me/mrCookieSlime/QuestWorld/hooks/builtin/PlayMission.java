package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

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
		return "&7Play for " + (instance.getAmount() / 60) + "h " + (instance.getAmount() % 60) + "m";
	}
	
	@Override
	public String progressString(float percent, int current, int total) {
		int remaining = total - current;
		return Math.round(percent * 100) + "% (" + (remaining / 60) + "h " + (remaining % 60) + "m remaining)";
	}

	@Override
	public int onTick(Player p, IMission mission) {
		return p.getStatistic(Statistic.PLAY_ONE_TICK) / 20 / 60;
	}
	
	@Override
	public int onManual(Player player, IMission mission) {
		return onTick(player, mission);
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(17, new MenuData(
				new ItemBuilder(Material.WATCH).display("&7Time: &b" + (changes.getAmount() / 60) + "h " + (changes.getAmount() % 60) + "m").lore(
						"",
						"&rLeft Click: &e+1m",
						"&rRight Click: &e-1m",
						"&rShift + Left Click: &e+1h",
						"&rShift + Right Click: &e-1h"
						).get(),
				MissionButton.simpleHandler(changes, event -> {
					int amount = MissionButton.clickNumber(changes.getAmount(), 60, event);
					if(amount < 1)
						amount = 1;
					changes.setAmount(amount);
				})
		));
	}
}
