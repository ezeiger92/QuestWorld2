package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

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
