package com.questworld.api.lang;

import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IPlayerStatus;
import com.questworld.util.Text;

public class MissionReplacements extends BaseReplacements<IMission> {
	private final IPlayerStatus status;
	private final IMission mission;
	
	public MissionReplacements(IMission mission) {
		this(null, mission);
	}
	
	public MissionReplacements(IPlayerStatus status, IMission mission) {
		super("mission.");
		this.status = status;
		this.mission = mission;
	}
	
	@Override
	public Class<IMission> forClass(){
		return IMission.class;
	}

	@Override
	public String getReplacement(String base, String fullKey) {
		switch (base) {
			case "desc":
			case "description":
			case "name":
				return mission.getDescription();
		}
		
		if (status == null) {
			return "";
		}
		
		switch (base) {
			case "progress":
				return Integer.toString(status.getProgress(mission));
			
			case "time":
				return Text.timeFromNum(mission.getTimeframe());
		}
		
		return "";
	}

}
