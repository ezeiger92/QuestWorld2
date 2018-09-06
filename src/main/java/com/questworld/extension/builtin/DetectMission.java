package com.questworld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.Ticking;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.ItemBuilder;
import com.questworld.util.Text;

public class DetectMission extends MissionType implements Ticking {

	public DetectMission() {
		super("DETECT", false, new ItemStack(Material.OBSERVER));
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getItem();
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Own " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}

	@Override
	public void onManual(Player p, MissionEntry result) {
		IMission mission = result.getMission();
		int amount = 0;
		for (int i = 0; i < 36; i++) {
			ItemStack current = p.getInventory().getItem(i);
			if (ItemBuilder.compareItems(current, mission.getItem()))
				amount = amount + current.getAmount();
		}

		if (amount >= mission.getAmount())
			result.setProgress(mission.getAmount());
	}

	@Override
	public String getLabel() {
		return "&r> Click to check for items";
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
