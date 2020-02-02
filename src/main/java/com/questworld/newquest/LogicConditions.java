package com.questworld.newquest;

import java.util.List;

import org.bukkit.event.Event;

import com.questworld.newquest.event.ConditionCheckEvent;

public class LogicConditions {
	private LogicConditions() {	
	}
	
	// Not a good instance holder
	public static class CondList extends Condition.Properties {
		public List<Integer> instanceIds;
	}
	
	public static class CondPair extends Condition.Properties {
		public int leftId;
		public int rightId;
	}
	
	public static class CondRef extends Condition.Properties {
		public int instanceId;
	}
	
	public static class And extends Condition {
		public And() {
			super(ConditionKey("and"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig<Condition.Properties> config, Profile profile) {
			ConditionCheckEvent event = (ConditionCheckEvent) someEvent;
			CondList list = config.deserialize(CondList.class);
			
			for(int i : list.instanceIds) {
				if(i == event.getInstanceId() && !event.getResult()) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public static class Or extends Condition {
		public Or() {
			super(ConditionKey("or"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig<Condition.Properties> config, Profile profile) {
			ConditionCheckEvent event = (ConditionCheckEvent) someEvent;
			CondList list = config.deserialize(CondList.class);
			
			for(int i : list.instanceIds) {
				if(i == event.getInstanceId() && event.getResult()) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	public static class Not extends Condition {
		public Not() {
			super(ConditionKey("not"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig<Condition.Properties> config, Profile profile) {
			ConditionCheckEvent event = (ConditionCheckEvent) someEvent;
			CondRef other = config.deserialize(CondRef.class);
			
			if(event.getInstanceId() == other.instanceId) {
				return !event.getResult();
			}
			
			return false;
		}
	}
	
	public static class Xor extends Condition {
		public Xor() {
			super(ConditionKey("xor"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig<Condition.Properties> config, Profile profile) {
			ConditionCheckEvent event = (ConditionCheckEvent) someEvent;
			CondPair rules = config.deserialize(CondPair.class);
			
			if(event.getInstanceId() == rules.leftId) {
				// A MESS; NOT HOW THIS WILL WORK
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.rightId));
				
				return other ^ event.getResult();
			}
			else if(event.getInstanceId() == rules.rightId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.leftId));
				
				return other ^ event.getResult();
			}
			
			// Not true or false: not applicable
			return false;
		}
	}
	
	public static class Iff extends Condition {
		public Iff() {
			super(ConditionKey("iff"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig<Condition.Properties> config, Profile profile) {
			ConditionCheckEvent event = (ConditionCheckEvent) someEvent;
			CondPair rules = config.deserialize(CondPair.class);
			
			if(event.getInstanceId() == rules.leftId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.rightId));
				
				return other ^ !event.getResult();
			}
			else if(event.getInstanceId() == rules.rightId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.leftId));
				
				return other ^ !event.getResult();
			}
			
			return true;
		}
	}
	
	public static class Implies extends Condition {
		public Implies() {
			super(ConditionKey("implies"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig<Condition.Properties> config, Profile profile) {
			ConditionCheckEvent event = (ConditionCheckEvent) someEvent;
			CondPair rules = config.deserialize(CondPair.class);
			
			if(event.getInstanceId() == rules.leftId) {
				if(!event.getResult()) {
					return true;
				}
				
				return Boolean.parseBoolean(profile.getData("state." + rules.rightId));
			}
			else if(event.getInstanceId() == rules.rightId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.leftId));
				
				if(!other) {
					return true;
				}
				
				return event.getResult();
			}
			
			return true;
		}
	}
}
