package me.mrCookieSlime.QuestWorld.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;

public class MissionSet implements Iterable<MissionEntry> {
	private List<MissionEntry> results;

	public MissionSet(PlayerStatus manager, MissionType type) {
		List<IMission> active = manager.getActiveMissions(type);
		results = new ArrayList<>(active.size());
		for(IMission mission : active)
			results.add(new Result(mission, manager));
	}
	
	public static class Result implements MissionEntry {
		private IMission mission;
		private PlayerStatus manager;
		
		public Result(IMission mission, PlayerStatus manager) {
			this.mission = mission;
			this.manager = manager;
		}
		
		public IMission getMission() {
			return mission;
		}
		
		public int getProgress() {
			return manager.getProgress(mission);
		}
		
		public void setProgress(int progress) {
			manager.setProgress(mission, progress);
		}
		
		public void addProgress(int progress) {
			manager.addProgress(mission, progress);
		}
		
		public int getRemaining() {
			return mission.getAmount() - getProgress();
		}
	}

	// TODO: Mission iterator, pulls results on request
	@Override
	public Iterator<MissionEntry> iterator() {
		return results.iterator();
	}
}
