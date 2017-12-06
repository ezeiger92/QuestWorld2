package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;

public class Builtin extends QuestExtension {
	private final MissionType[] missions = {
		new CraftMission(),
		new SubmitMission(),
		new DetectMission(),
		new KillMission(),
		new KillNamedMission(),
		new FishMission(),
		new LocationMission(),
		new JoinMission(),
		new PlayMission(),
		new MineMission(),
		new LevelMission(),
	};

	@Override
	public String[] getDepends() {
		return null;
	}

	@Override
	public void initialize(Plugin parent) {
	}

	@Override
	public MissionType[] getMissions() {
		return missions;
	}

}
