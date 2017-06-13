package me.mrCookieSlime.QuestWorld.hooks.votifier;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;

public class VotifierHook extends QuestExtension {
	@Override
	public String[] getDepends() {
		return new String[] { "ChatReaction" };
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
