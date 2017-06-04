package me.mrCookieSlime.QuestWorld.hooks;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.listeners.TaskListener;
import me.mrCookieSlime.QuestWorld.quests.missions.*;
import me.mrCookieSlime.QuestWorld.utils.Log;

public class BuiltinHook extends QuestExtension {
	private MissionType[] missions;

	@Override
	public String[] getDepends() {
		return null;
	}

	@Override
	public void initialize(Plugin parent) {
		Log.info("Creating built-in missions");
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
