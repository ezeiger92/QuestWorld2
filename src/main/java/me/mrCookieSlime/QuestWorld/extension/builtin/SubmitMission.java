package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionSet;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.Text;

public class SubmitMission extends MissionType implements Manual {
	public SubmitMission() {
		super("SUBMIT", false, new ItemStack(Material.CHEST));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getMissionItem();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Submit " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}

	@Override
	public void onManual(Player p, MissionSet.Result result) {
		IMission mission = result.getMission();
		int found = result.getRemaining();
		
		ItemStack search = mission.getMissionItem();
		search.setAmount(found);
		
		ItemStack missing = p.getInventory().removeItem(search).get(0);
		if(missing != null)
			found -= missing.getAmount();
		
		if(found > 0) {
			QuestWorld.getSounds().MISSION_SUBMIT.playTo(p);
			// TODO QuestWorld.getSounds().muteNext();
			result.addProgress(found);
		}
		else {
			QuestWorld.getSounds().MISSION_REJECT.playTo(p);
		}
	}
	
	@Override
	public String getLabel() {
		return "Submit";
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
