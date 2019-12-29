package com.questworld.extension.builtin;

import com.questworld.adapter.CurrentAdapter;
import com.questworld.adapter.CurrentSpigotAdapter;
import com.questworld.api.QuestExtension;
import com.questworld.util.Reflect;

public class Builtin extends QuestExtension {
	public Builtin() {
		setMissionTypes(new CraftMission(), new SubmitMission(), new DetectMission(), new KillMission(),
				new KillNamedMission(), new FishMission(), new LocationMission(), new JoinMission(), new PlayMission(),
				new MineMission(), new LevelMission(), new FarmMission());

		Reflect.addAdapter(new CurrentAdapter());
		try {
			Reflect.addAdapter(new CurrentSpigotAdapter());
		}
		catch (ClassNotFoundException e) {
		}
	}
}
