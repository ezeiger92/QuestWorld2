package me.mrCookieSlime.QuestWorld.api;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.util.Log;

public class UnknownMission extends MissionType {
	
	private static HashMap<String, UnknownMission> cache = new HashMap<>();
	public static UnknownMission get(String name) {
		UnknownMission result = cache.get(name);
		if(result == null) {
			result = new UnknownMission(name);
			cache.put(name, result);
		}
		Log.warning("Tried to fetch unknown mission type: " + name + ". Did an extension fail to load?");
		Log.warning("Supplying dummy mission for " + name);
		return result;
	}

	private UnknownMission(String name) {
		super(name, false, false, new ItemStack(Material.BARRIER));
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
