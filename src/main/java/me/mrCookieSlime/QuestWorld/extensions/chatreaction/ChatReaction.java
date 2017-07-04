package me.mrCookieSlime.QuestWorld.extensions.chatreaction;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;

public class ChatReaction extends QuestExtension {

	@Override
	public String[] getDepends() {
		return new String[] { "ChatReaction" };
	}

	MissionType mission;
	@Override
	public void initialize(Plugin parent) {
		mission = new ChatReactMission();
	}

	@Override
	public MissionType[] getMissions() {
		return new MissionType[] { mission };
	}
}
