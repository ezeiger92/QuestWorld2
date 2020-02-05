package com.questworld.newquest;

import com.questworld.util.UniqueKey;

public abstract class Reward {

	private static final String NAMESPACE = "questworld-reward";
	private final UniqueKey ID;
	
	private static final ConfigDB<Reward, Properties> database = new ConfigDB<>();
	
	public abstract boolean apply(NodeConfig<Properties> properties, Profile profile);
	
	protected Reward(UniqueKey ID) {
		this.ID = ID;
	}
	
	protected static UniqueKey RewardKey(String key) {
		return new UniqueKey(NAMESPACE, key);
	}
	
	public UniqueKey getID() {
		return ID;
	}
	
	@Override
	public String toString() {
		return ID.toString();
	}
	
	public static class Properties {
		public int id;
		
	}
}
