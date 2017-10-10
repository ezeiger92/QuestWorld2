package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;

public class DetectMission extends MissionType implements Ticking {
	public DetectMission() {
		super("DETECT", false, false, new ItemStack(Material.OBSERVER));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Own " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}

	@Override
	public int onManual(Player p, IMission mission) {
		int amount = 0;
		for (int i = 0; i < 36; i++) {
			ItemStack current = p.getInventory().getItem(i);
			if (QuestWorld.getInstance().isItemSimiliar(current, mission.getMissionItem())) amount = amount + current.getAmount();
		}
		
		if (amount >= mission.getAmount()) {
			return amount;
		}
		
		return FAIL;
	}

	@Override
	public String getLabel() {
		return "Detect";
	}
	
	@Override
	protected void layoutMenu(IMissionWrite changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
