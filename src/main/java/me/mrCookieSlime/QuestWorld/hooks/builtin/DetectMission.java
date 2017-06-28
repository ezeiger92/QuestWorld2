package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;

public class DetectMission extends MissionType implements Manual {
	public DetectMission() {
		super("DETECT", false, false, new MaterialData(Material.OBSERVER));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Own " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}

	@Override
	public int onManual(PlayerManager manager, IMission mission) {
		Player p = Bukkit.getPlayer(manager.getUUID());
		
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
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
