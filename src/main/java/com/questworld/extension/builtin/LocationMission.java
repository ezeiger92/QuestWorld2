package com.questworld.extension.builtin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.SinglePrompt;
import com.questworld.api.Ticking;
import com.questworld.api.Translation;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.IPlayerStatus;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.event.GenericPlayerLeaveEvent;
import com.questworld.api.menu.MenuData;
import com.questworld.api.menu.MissionButton;
import com.questworld.api.menu.QuestBook;
import com.questworld.util.ItemBuilder;
import com.questworld.util.PlayerTools;
import com.questworld.util.Text;

public class LocationMission extends MissionType implements Ticking {
	public LocationMission() {
		super("REACH_LOCATION", false, new ItemStack(Material.LEATHER_BOOTS));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemBuilder(getSelectorItem()).display(Text.stringOf(instance.getLocation())).flagAll().get();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		Location location = instance.getLocation();
		String locationName = instance.getCustomString();
		if(locationName.isEmpty())
			locationName = Text.stringOf(location);
			
		return "&7Travel to " + locationName;
	}
	
	@Override
	public void validate(IMissionState missionState) {
		int oldStyleRadius = missionState.getAmount();
		int radius = missionState.getCustomInt();
		
		if(oldStyleRadius != 1) {
			missionState.setAmount(1);
			if(radius == 0) {
				missionState.setCustomInt(oldStyleRadius);
				missionState.apply();
			}
		}
		
		// Minimum radius is 1, fixes ezeiger92/QuestWorld2#35	
		if(radius <= 0) {
			missionState.setCustomInt(3);
			missionState.apply();
		}
	}
	
	protected double worldDistance(Location left, Location right, int radius) {
		if(left.getWorld() != right.getWorld())
			return Double.MAX_VALUE;
		
		return left.distanceSquared(right) - radius * radius;
	}
	
	private HashMap<UUID, HashSet<UUID>> close = new HashMap<>();
	
	@Override
	public void onManual(Player p, MissionEntry entry) {
		HashSet<UUID> closeMissions = close.get(p.getUniqueId());
		
		IMission mission = entry.getMission();
		double distance = worldDistance(mission.getLocation(), p.getLocation(), mission.getCustomInt());
		if(distance < 0)
			entry.addProgress(1);
		
		if(distance < 64 * 64) {
			if(closeMissions == null) {
				closeMissions = new HashSet<>();
				close.put(entry.getMission().getUniqueId(), closeMissions);
			}
			closeMissions.add(entry.getMission().getUniqueId());
		}
		else if(closeMissions != null) {
			closeMissions.remove(entry.getMission().getUniqueId());
			if(closeMissions.isEmpty())
				close.remove(p.getUniqueId());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		HashSet<UUID> closeMissions = close.get(p.getUniqueId());
		
		if(closeMissions != null && !closeMissions.isEmpty()) {
			IPlayerStatus status = QuestWorld.getPlayerStatus(p);
			Location ploc = p.getLocation();
			
			for(UUID missionUniqueId : closeMissions) {
				IMission mission = QuestWorld.getFacade().getMission(missionUniqueId);
				
				if(worldDistance(mission.getLocation(), ploc, mission.getCustomInt()) < 0
						&& status.isMissionActive(mission))
					QuestWorld.getMissionEntry(mission, event.getPlayer()).addProgress(1);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(GenericPlayerLeaveEvent event) {
		close.remove(event.getPlayer().getUniqueId());
	}
	
	@Override
	public String getLabel() {
		return "&r> Click to check your position";
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		String name = changes.getCustomString();
		putButton(10, MissionButton.location(changes));
		putButton(11, new MenuData(
				new ItemBuilder(Material.NAME_TAG).wrapText(
						"&7Location name: &r&o" + (name.length() > 0 ? name : "-none-"),
						 "",
						 "&e> Give your location a name",
						 "",
						 "&rLeft click: Enter name",
						 "&rRight click: Reset name").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
					
					if(event.isRightClick()) {
						changes.setCustomString("");
						
						if(changes.apply()) {
							PlayerTools.sendTranslation(p, true, Translation.LOCMISSION_NAME_SET);
							QuestBook.openQuestMissionEditor(p, changes.getSource());
						}
						return;
					}
					
					p.closeInventory();
					PlayerTools.promptInput(p, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.LOCMISSION_NAME_EDIT),
							(c,s) -> {
								changes.setCustomString(Text.deserializeNewline(Text.colorize(s)));
								
								if(changes.apply()) {
									PlayerTools.sendTranslation(p, true, Translation.LOCMISSION_NAME_SET);
									QuestBook.openQuestMissionEditor(p, changes.getSource());
								}

								
								return true;
							}
					));
				}
		));
		putButton(17, MissionButton.simpleButton(
				changes,
				new ItemBuilder(Material.COMPASS).wrapText(
						"&7Radius: &a" + changes.getCustomInt(),
						"",
						"&rLeft click: &e+1",
						"&rRight click: &e-1",
						"&rShift left click: &e+16",
						"&rShift right click: &e-16").get(),
				event -> {
					int amount = MissionButton.clickNumber(changes.getCustomInt(), 16, event);
					changes.setCustomInt(Math.max(amount, 1));
				}
		));
	}
}
