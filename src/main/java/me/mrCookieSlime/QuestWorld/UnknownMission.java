package me.mrCookieSlime.QuestWorld;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.util.Log;

public class UnknownMission extends MissionType {
	private static HashMap<String, UnknownMission> unknown = new HashMap<>();
	public static UnknownMission get(String name) {
		UnknownMission mission = unknown.get(name);
		
		if(mission != null)
			return mission;
		
		Log.warning("Tried to fetch unknown mission type: " + name + ". Did an extension fail to load?");
		Log.warning("Supplying dummy mission for " + name);
		mission = new UnknownMission(name);
		unknown.put(name, mission);
		return mission;
	}

	private UnknownMission(String name) {
		super(name, false, new ItemStack(Material.BARRIER));
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		return "&rUnknown mission type: " + getName() + "! Contact an admin!";
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return getSelectorItem().clone();
	}
}
