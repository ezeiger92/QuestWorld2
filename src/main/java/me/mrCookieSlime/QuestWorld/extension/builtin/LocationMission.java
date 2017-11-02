package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.api.menu.QuestBook;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;

public class LocationMission extends MissionType implements Ticking {
	public LocationMission() {
		super("REACH_LOCATION", false, new ItemStack(Material.LEATHER_BOOTS));
	}
	
	public static String coordinateString(Location location) {
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		return "X: "+ x +", Y: "+ y +", Z: "+ z;
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemBuilder(getSelectorItem()).display(coordinateString(instance.getLocation())).get();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		Location location = instance.getLocation();
		String locationName = instance.getCustomString();
		if(locationName.isEmpty())
			locationName = coordinateString(location);
			
		return "&7Travel to " + locationName;
	}
	
	@Override
	public boolean attemptUpgrade(IMissionState instance) {
		int oldStyleRadius = instance.getAmount();
		if(oldStyleRadius > 1) {
			instance.setAmount(1);
			instance.setCustomInt(oldStyleRadius);
			return true;
		}
		
		// Minimum radius is 1, fixes ezeiger92/QuestWorld2#35	
		if(instance.getCustomInt() <= 0)
			instance.setCustomInt(3);
		
		return false;
	}
	
	@Override
	public int onManual(Player p, IMission mission) {
		if (mission.getLocation().getWorld().getName().equals(p.getWorld().getName())
				&& mission.getLocation().distanceSquared(p.getLocation()) < mission.getCustomInt() * mission.getCustomInt()) {
			return 1;
		}
		
		return FAIL;
	}
	
	@Override
	public String getLabel() {
		return "Detect";
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.location(changes));
		putButton(11, new MenuData(
				new ItemBuilder(Material.NAME_TAG).display("&r" + changes.getCustomString()).lore(
						 "",
						 "&e> Give your Location a Name").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
					
					PlayerTools.promptInput(p, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.LOCMISSION_NAME_EDIT),
							(c,s) -> {
								changes.setCustomString(Text.colorize(s));
								
								if(changes.apply()) {
									PlayerTools.sendTranslation(p, true, Translation.LOCMISSION_NAME_SET);
								}

								QuestBook.openQuestMissionEditor(p, changes.getSource());
								return true;
							}
					));

					PlayerTools.closeInventoryWithEvent(p);
				}
		));
		putButton(17, new MenuData(
				new ItemBuilder(Material.COMPASS).display("&7Radius: &a" + changes.getCustomInt()).lore(
						"",
						"&rLeft Click: &e+1",
						"&rRight Click: &e-1",
						"&rShift + Left Click: &e+16",
						"&rShift + Right Click: &e-16").get(),
				event -> {
					int amount = MissionButton.clickNumber(changes.getCustomInt(), 16, event);
					changes.setCustomInt(Math.max(amount, 1));
					MissionButton.apply(event, changes);
				}
		));
	}
}
