package me.mrCookieSlime.QuestWorld.extensions.builtin;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.listeners.TaskListener;

public class Builtin extends QuestExtension {
	private MissionType[] missions;

	@Override
	public String[] getDepends() {
		return null;
	}

	@Override
	public void initialize(Plugin parent) {
		missions = new MissionType[] {
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
		
		// Listeners for remaining (kill) quests that need some work
		new TaskListener(parent);
	}

	@Override
	public MissionType[] getMissions() {
		return missions;
	}

}
