package com.questworld.extension.builtin;

import com.questworld.api.QuestExtension;

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
