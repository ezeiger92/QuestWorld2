package me.mrCookieSlime.QuestWorld.api.contract;

public interface MissionEntry {
	public IMission getMission();
	public int getProgress();
	public int getRemaining();
	public void addProgress(int progress);
	public void setProgress(int progress);
}
