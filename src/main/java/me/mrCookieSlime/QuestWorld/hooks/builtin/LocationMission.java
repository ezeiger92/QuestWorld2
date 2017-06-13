package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMissionWrite;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;

public class LocationMission extends MissionType {
	public LocationMission() {
		super("REACH_LOCATION", false, false, true, SubmissionType.LOCATION, new MaterialData(Material.LEATHER_BOOTS));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return SubmissionItemResolver.location(Material.LEATHER_BOOTS, instance.getLocation());
	}
	
	@Override
	protected String displayString(IMission instance) {
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
		
		return false;
	}
}
