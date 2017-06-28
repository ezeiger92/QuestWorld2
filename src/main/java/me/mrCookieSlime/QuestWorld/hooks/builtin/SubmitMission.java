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

public class SubmitMission extends MissionType implements Manual {
	public SubmitMission() {
		super("SUBMIT", false, false, new MaterialData(Material.CHEST));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String displayString(IMission instance) {
		return "&7Submit " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false);
	}

	@Override
	public int onManual(PlayerManager manager, IMission mission) {
		Player p = Bukkit.getPlayer(manager.getUUID());
		int current =  manager.getProgress(mission);
		int needed = mission.getAmount() - current;
		int found = needed;
		
		ItemStack search = mission.getMissionItem().clone();
		search.setAmount(needed);
		
		ItemStack missing = p.getInventory().removeItem(search).get(0);
		if(missing != null)
			found = needed - missing.getAmount();
		
		if(found > needed) {
			QuestWorld.getSounds().MissionSubmit().playTo(p);
			QuestWorld.getSounds().muteNext();
			
			return current + found;
		}
		else {
			QuestWorld.getSounds().MissionReject().playTo(p);
			return FAIL;
		}
	}
	
	@Override
	public String getLabel() {
		return "Submit";
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
