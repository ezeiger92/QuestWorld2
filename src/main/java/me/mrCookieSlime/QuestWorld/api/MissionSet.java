package me.mrCookieSlime.QuestWorld.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;

public class MissionSet implements Iterable<MissionSet.Result> {
	private List<Result> results;
	
	public static MissionSet of(MissionType type, Player player) {
		return new MissionSet(PlayerManager.of(player), type); 
	}
	
	public static MissionSet of(MissionType type, UUID uuid) {
		return new MissionSet(PlayerManager.of(uuid), type); 
	}
	
	private MissionSet(PlayerManager manager, MissionType type) {
		List<IMission> active = manager.getActiveMissions(type);
		results = new ArrayList<>(active.size());
		for(IMission mission : active)
			results.add(new Result(mission, manager.getProgress(mission)));
	}
	
	public static class Result {
		private IMission mission;
		private int progress;
		
		public Result(IMission mission, int progress) {
			this.mission = mission;
			this.progress = progress;
		}
		
		public IMission getMission() {
			return mission;
		}
		
		public int getProgress() {
			return progress;
		}
		
		public void setProgress(int progress) {
			this.progress = progress;
		}
		
		public void addProgress(int progress) {
			this.progress += progress;
		}
		
		public int getRemaining() {
			return mission.getAmount() - getProgress();
		}
	}

	@Override
	public Iterator<Result> iterator() {
		return results.iterator();
	}
}
