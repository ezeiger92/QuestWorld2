package com.questworld.newquest;

import org.bukkit.event.Event;

import com.questworld.util.UniqueKey;

public abstract class Condition {
	private static final String NAMESPACE = "questworld-condition";
	private final UniqueKey ID;
	
	// All live instances of rules, should not be stored here
	// Only for mental model
	private static final ConfigDB<Condition, Properties> database = new ConfigDB<>();
	
	protected void testConditions(Event someEvent) {
		for(NodeConfig<Properties> config : database.getConfigs(getClass())) {
			// Need to get association of rule&config&player
			test(someEvent, config, new Profile());
		}
	}
	
	public abstract boolean test(Event someEvent, NodeConfig<Properties> config, Profile profile);
	
	protected Condition(UniqueKey ID) {
		this.ID = ID;
	}
	
	protected static UniqueKey ConditionKey(String key) {
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
