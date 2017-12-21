package me.mrCookieSlime.QuestWorld.extension.builtin;

import me.mrCookieSlime.QuestWorld.api.QuestExtension;

public class Builtin extends QuestExtension {
	public Builtin() {
		setMissionTypes(
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
			new LevelMission());
	}
}
