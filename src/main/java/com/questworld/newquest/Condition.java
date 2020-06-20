package com.questworld.newquest;

import org.bukkit.event.Event;

import com.questworld.util.UniqueKey;

public abstract class Condition {
	private static final String NAMESPACE = "questworld-condition";
	private final UniqueKey ID;
	
	// All live instances of rules, should not be stored here
	// Only for mental model
	private static final ConfigDB<Condition, BaseProperties> database = new ConfigDB<>();
	
	protected final void testConditions(Event someEvent) {
		for(NodeConfig<BaseProperties> config : database.getConfigs(getClass())) {
			Profile profile = new Profile();
			
			BaseProperties props = config.deserialize(BaseProperties.class);
			
			if(profile.isTracking(props.id)) {
				test(someEvent, config, profile);
			}
		}
	}
	
	public abstract boolean test(Event someEvent, NodeConfig<BaseProperties> config, Profile profile);
	
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
	
	public static class BaseProperties {
		public int id;
		
	}
}
