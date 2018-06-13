package com.questworld.manager;

import java.util.Iterator;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.MissionEntry;

public class MissionSet implements Iterable<MissionEntry> {
	private final MissionType type;
	private final PlayerStatus manager;

	public MissionSet(MissionType type, PlayerStatus manager) {
		this.manager = manager;
		this.type = type;
	}

	public static class Result implements MissionEntry {
		private final IMission mission;
		private final PlayerStatus manager;

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

	public static class MissionEntryIterator implements Iterator<MissionEntry> {
		private final Iterator<IMission> missionIter;
		private final PlayerStatus playerStatus;

		private MissionEntry nextEntry;
		private boolean hadNext;

		public MissionEntryIterator(Iterator<IMission> missionIter, PlayerStatus playerStatus) {
			this.missionIter = missionIter;
			this.playerStatus = playerStatus;

			next();
		}

		@Override
		public boolean hasNext() {
			return hadNext;
		}

		@Override
		public MissionEntry next() {
			MissionEntry result = nextEntry;

			hadNext = false;
			while (missionIter.hasNext()) {
				IMission mission = missionIter.next();
				if (playerStatus.isMissionActive(mission)) {
					hadNext = true;
					nextEntry = new Result(mission, playerStatus);
					break;
				}
			}

			return result;
		}
	}

	@Override
	public Iterator<MissionEntry> iterator() {
		return new MissionEntryIterator(QuestWorld.getViewer().getMissionsOf(type).iterator(), manager);
	}
}
