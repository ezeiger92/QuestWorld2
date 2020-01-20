package com.questworld.extension.builtin;

import com.questworld.adapter.CurrentAdapter;
import com.questworld.api.QuestExtension;
import com.questworld.util.Reflect;

public class Builtin extends QuestExtension {
	public Builtin() {
		setMissionTypes(
				new CraftMission(),
				new DetectMission(),
				new FishMission(),
				new HarvestMission(),
				new JoinMission(),
				new LevelMission(),
				new LocationMission(),
				new KillMission(),
				new KillNamedMission(),
				new MineMission(),
				new PlayMission(),
				new SubmitMission());

		Reflect.addAdapter(new CurrentAdapter());
	}
}
