package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;

public class LocationMission extends MissionType implements Ticking, Manual {
	public LocationMission() {
		super("REACH_LOCATION", false, false, new ItemStack(Material.LEATHER_BOOTS));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return SubmissionItemResolver.location(Material.LEATHER_BOOTS, instance.getLocation());
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		Location loc = instance.getLocation();
		String locStr = instance.getName();
		if(locStr.isEmpty())
			locStr = String.format("X: %d, Y: %d, Z: %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			
		return "&7Travel to " + locStr;
	}
	
	@Override
	public boolean attemptUpgrade(IMissionWrite instance) {
		int oldStyleRadius = instance.getAmount();
		if(oldStyleRadius > 1) {
			instance.setAmount(1);
			instance.setCustomInt(oldStyleRadius);
			return true;
		}
		
		// Minimum radius is 1, fixes ezeiger92/QuestWorld2#35	
		if(instance.getCustomInt() <= 0)
			instance.setCustomInt(1);
		
		return false;
	}

	@Override
	public int onTick(Player p, IMission mission) {
		if (mission.getLocation().getWorld().getName().equals(p.getWorld().getName())
				&& mission.getLocation().distanceSquared(p.getLocation()) < mission.getCustomInt() * mission.getCustomInt()) {
			return 1;
		}
		
		return FAIL;
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
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.location(changes));
		putButton(11, new MenuData(
				new ItemBuilder(Material.NAME_TAG).display("&r" + changes.getEntityName()).lore(
						 "",
						 "&e> Give your Location a Name").get(),
				MissionButton.simpleHandler(changes, event -> {
					Player p = (Player)event.getWhoClicked();
					QuestWorld.getInstance().storeInput(p.getUniqueId(), new Input(InputType.LOCATION_NAME, changes.getSource()));
					PlayerTools.sendTranslation(p, true, Translation.location_rename);
					p.closeInventory();
				})
		));
		putButton(17, new MenuData(
				new ItemBuilder(Material.COMPASS).display("&7Radius: &a" + changes.getCustomInt()).lore(
						"",
						"&rLeft Click: &e+1",
						"&rRight Click: &e-1",
						"&rShift + Left Click: &e+16",
						"&rShift + Right Click: &e-16").get(),
				MissionButton.simpleHandler(changes, event -> {
					int amount = MissionButton.clickNumber(changes.getAmount(), 16, event);
					changes.setCustomInt(Math.max(amount, 1));
				})
		));
	}
}
