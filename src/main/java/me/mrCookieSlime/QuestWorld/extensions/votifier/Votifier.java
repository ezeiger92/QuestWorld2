package me.mrCookieSlime.QuestWorld.extensions.votifier;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;

public class Votifier extends QuestExtension {
	@Override
	public String[] getDepends() {
		return new String[] { "Votifier" };
	}

	MissionType[] missions = null;
	@Override
	public void initialize(Plugin parent) {
		missions = new MissionType[] {
			new VoteMission()
		}; 
	}

	@Override
	public MissionType[] getMissions() {
		return missions;
	}
}
