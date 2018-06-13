package com.questworld.quest;

import java.util.UUID;

class UniqueObject {
	private UUID uniqueId = UUID.randomUUID();

	public final UUID getUniqueId() {
		return uniqueId;
	}

	protected final void setUniqueId(String uuid) {
		if (uuid != null)
			try {
				uniqueId = UUID.fromString(uuid);
			}
			catch (IllegalArgumentException e) {
			}
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
		return getUniqueId().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof UniqueObject && uniqueId.equals(((UniqueObject) o).uniqueId);
	}
}
