package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.Text;

public class SubmitMission extends MissionType implements Manual {
	public SubmitMission() {
		super("SUBMIT", false, false, new ItemStack(Material.CHEST));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getMissionItem().clone();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Submit " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}

	@Override
	public int onManual(Player p, IMission mission) {
		int current =  QuestWorld.getInstance().getManager(p).getProgress(mission);
		int needed = mission.getAmount() - current;
		int found = needed;
		
		ItemStack search = mission.getMissionItem().clone();
		search.setAmount(needed);
		
		ItemStack missing = p.getInventory().removeItem(search).get(0);
		if(missing != null)
			found -= missing.getAmount();
		
		if(found > 0) {
			QuestWorld.getSounds().MissionSubmit().playTo(p);
			// TODO QuestWorld.getSounds().muteNext();
			
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
	protected void layoutMenu(IMissionWrite changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
