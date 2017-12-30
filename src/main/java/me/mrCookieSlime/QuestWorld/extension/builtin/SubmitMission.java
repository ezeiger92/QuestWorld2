package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

public class SubmitMission extends MissionType implements Manual {
	public SubmitMission() {
		super("SUBMIT", false, new ItemStack(Material.CHEST));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return instance.getItem();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&7Submit " + instance.getAmount() + "x " + Text.itemName(instance.getDisplayItem());
	}

	@Override
	public void onManual(Player p, MissionEntry entry) {
		IMission mission = entry.getMission();
		int needed = entry.getRemaining();
		
		ItemStack search = mission.getItem();
		search.setAmount(needed);
		for(ItemStack stack : p.getInventory().getStorageContents()) {
			if(ItemBuilder.compareItems(mission.getItem(), stack)) {
				int sa = search.getAmount();
				int sub = Math.min(stack.getAmount(), sa);
				stack.setAmount(stack.getAmount() - sub);
				search.setAmount(sa - sub);
				
				if(sa == 0)
					break;
			}
		}
		
		//ItemStack missing = p.getInventory().removeItem(search).get(0);
		//if(missing != null)
		//	remaining -= missing.getAmount();
		
		if(needed > search.getAmount()) {
			QuestWorld.getSounds().MISSION_SUBMIT.playTo(p);
			// TODO QuestWorld.getSounds().muteNext();
			entry.addProgress(needed - search.getAmount());
		}
		else {
			QuestWorld.getSounds().MISSION_REJECT.playTo(p);
		}
	}
	
	@Override
	public String getLabel() {
		return "&r> Click to submit items";
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, MissionButton.item(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
