package me.mrCookieSlime.QuestWorld.extension.builtin;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.event.GenericPlayerLeaveEvent;
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
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return new ItemBuilder(getSelectorItem()).display(Text.stringOf(instance.getLocation())).get();
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
			if(radius == 0)
				missionState.setCustomInt(oldStyleRadius);
		}
		
		// Minimum radius is 1, fixes ezeiger92/QuestWorld2#35	
		if(radius <= 0)
			missionState.setCustomInt(3);
		
		missionState.apply();
	}
	
	protected boolean withinRadius(Location left, Location right, int radius) {
		return left.getWorld() == right.getWorld() && left.distanceSquared(right) <= radius * radius;
	}
	
	@Override
	public void onManual(Player p, MissionEntry entry) {
		IMission mission = entry.getMission();
		if(withinRadius(mission.getLocation(), p.getLocation(), mission.getCustomInt()))
			entry.addProgress(1);
	}
	
	private HashMap<Player, Double> distanceMap = new HashMap<>();
	
	/*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		double moved = event.getFrom().distanceSquared(event.getTo());
		
		// Looking around
		if(moved == 0)
			return;
		
		Player player = event.getPlayer();
		double distance = distanceMap.getOrDefault(player, 0.0);
		
		// Handle big moves (teleports)
		if(distance > moved * 100) {
			distanceMap.put(player, distance * 0.0000001);
			return;
		}
		
		double fdist = 1000000000000.0;
		
		for(MissionEntry entry : QuestWorld.getMissionEntries(this, player)) {
			Location missionLoc = entry.getMission().getLocation();
			if(missionLoc.getWorld() != player.getWorld())
				continue;
			
			int radSquared = entry.getMission().getCustomInt() * entry.getMission().getCustomInt();
			
			double difference = missionLoc.distanceSquared(player.getLocation());
			if(radSquared < difference) {
				fdist = Math.min(fdist, difference);
			}
			else
				entry.addProgress(1);
		}
		
		fdist = Math.pow(fdist, 12);
		distanceMap.put(player, fdist);
	}*/
	
	@EventHandler
	public void onPlayerLeave(GenericPlayerLeaveEvent event) {
		distanceMap.remove(event.getPlayer());
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
								changes.setCustomString(Text.colorize(s));
								
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
