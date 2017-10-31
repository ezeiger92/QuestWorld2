package me.mrCookieSlime.QuestWorld.quest;

class Renderable {
	private static long s_uniqueId = 0;
	
	private final long uniqueId = s_uniqueId++;
	public final long getUnique() {
		return uniqueId;
	}
	
	private long lastModified = System.currentTimeMillis();
	public long getLastModified() {
		return lastModified;
	}
	
	public void updateLastModified() {
		lastModified = System.currentTimeMillis();
	}

	@Override
	public int hashCode() {
		return (int)getUnique();
	}
}
