package com.questworld.newquest;

import java.util.UUID;

public class Profile {
	private final UUID guid;
	public Profile() {
		guid = UUID.randomUUID();
	}
	
	public UUID getUniqueId() {
		return guid;
	}
	
	public void storeData(String key, String value) {
	}
	
	public String getData(String key) {
		return "";
	}
	
	public boolean isTracking(int conditionId) {
		return true;
	}
}
