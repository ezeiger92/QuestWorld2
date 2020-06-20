package com.questworld;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.api.contract.IMission;
import com.questworld.util.Log;

public class UnknownMission extends MissionType {
	private static HashMap<String, UnknownMission> unknown = new HashMap<>();

	public static UnknownMission get(String name) {
		UnknownMission mission = unknown.get(name);

		if (mission != null)
			return mission;

		Log.warning("Tried to fetch unknown mission type: " + name + ". Did an extension fail to load?");
		Log.warning("Supplying dummy mission for " + name);
		mission = new UnknownMission(name);
		unknown.put(name, mission);
		return mission;
	}

	private UnknownMission(String name) {
		super(name, false, null);
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		
		return QuestWorld.translate(Translation.TYPE_UNKNOWN, getName());
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return getSelectorItem().clone();
	}
}
