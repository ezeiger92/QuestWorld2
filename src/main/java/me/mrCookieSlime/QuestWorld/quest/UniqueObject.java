package me.mrCookieSlime.QuestWorld.quest;

class UniqueObject {
	private static long s_uniqueId = 0;
	
	private final long uniqueId = s_uniqueId++;
	public final long getUnique() {
		return uniqueId;
	}
	
	protected final void setUnique(long number) {
		s_uniqueId = lastModified = number;
		++s_uniqueId;
	}
	
	private long lastModified = System.currentTimeMillis();
	public long getLastModified() {
		return lastModified;
	}
	
	protected void updateLastModified() {
		lastModified = System.currentTimeMillis();
	}

	@Override
	public int hashCode() {
		return (int)getUnique();
	}
}
