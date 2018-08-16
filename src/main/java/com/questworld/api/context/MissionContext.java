package com.questworld.api.context;

import com.questworld.api.contract.IMission;

public class MissionContext extends Context {
	public MissionContext(IMission mission) {
		this(mission, "mission");
	}
	
	public MissionContext(IMission mission, String prefix) {
		super(prefix);

		map(null, mission::getText);
		map("name", mission::getText);
		map("amount", mission::getAmount);
		
		mapAll(new QuestContext(mission.getQuest()).getMapping(), true);
	}
}
