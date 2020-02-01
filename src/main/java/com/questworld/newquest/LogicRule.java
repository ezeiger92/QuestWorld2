package com.questworld.newquest;

import java.util.List;

import org.bukkit.event.Event;

import com.questworld.newquest.event.RuleResultEvent;

public class LogicRule {
	private LogicRule() {	
	}
	
	// Not a good instance holder
	public static class RuleList {
		public List<Integer> instanceIds;
	}
	
	public static class RulePair {
		public int leftId;
		public int rightId;
	}
	
	public static class RuleRef {
		public int instanceId;
	}
	
	public static class And extends Rule {
		public And() {
			super(MakeRuleKey("and"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig config, Profile profile) {
			RuleResultEvent event = (RuleResultEvent) someEvent;
			RuleList list = config.deserialize(RuleList.class);
			
			for(int i : list.instanceIds) {
				if(i == event.getInstanceId() && !event.getResult().isAllowed()) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public static class Or extends Rule {
		public Or() {
			super(MakeRuleKey("or"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig config, Profile profile) {
			RuleResultEvent event = (RuleResultEvent) someEvent;
			RuleList list = config.deserialize(RuleList.class);
			
			for(int i : list.instanceIds) {
				if(i == event.getInstanceId() && event.getResult().isAllowed()) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	public static class Not extends Rule {
		public Not() {
			super(MakeRuleKey("not"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig config, Profile profile) {
			RuleResultEvent event = (RuleResultEvent) someEvent;
			RuleRef other = config.deserialize(RuleRef.class);
			
			if(event.getInstanceId() == other.instanceId) {
				return !event.getResult().isAllowed();
			}
			
			return false;
		}
	}
	
	public static class Xor extends Rule {
		public Xor() {
			super(MakeRuleKey("xor"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig config, Profile profile) {
			RuleResultEvent event = (RuleResultEvent) someEvent;
			RulePair rules = config.deserialize(RulePair.class);
			
			if(event.getInstanceId() == rules.leftId) {
				// A MESS; NOT HOW THIS WILL WORK
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.rightId));
				
				return other ^ event.getResult().isAllowed();
			}
			else if(event.getInstanceId() == rules.rightId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.leftId));
				
				return other ^ event.getResult().isAllowed();
			}
			
			// Not true or false: not applicable
			return false;
		}
	}
	
	public static class Iff extends Rule {
		public Iff() {
			super(MakeRuleKey("iff"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig config, Profile profile) {
			RuleResultEvent event = (RuleResultEvent) someEvent;
			RulePair rules = config.deserialize(RulePair.class);
			
			if(event.getInstanceId() == rules.leftId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.rightId));
				
				return other ^ !event.getResult().isAllowed();
			}
			else if(event.getInstanceId() == rules.rightId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.leftId));
				
				return other ^ !event.getResult().isAllowed();
			}
			
			return true;
		}
	}
	
	public static class Implies extends Rule {
		public Implies() {
			super(MakeRuleKey("implies"));
		}

		@Override
		public boolean test(Event someEvent, NodeConfig config, Profile profile) {
			RuleResultEvent event = (RuleResultEvent) someEvent;
			RulePair rules = config.deserialize(RulePair.class);
			
			if(event.getInstanceId() == rules.leftId) {
				if(!event.getResult().isAllowed()) {
					return true;
				}
				
				return Boolean.parseBoolean(profile.getData("state." + rules.rightId));
			}
			else if(event.getInstanceId() == rules.rightId) {
				boolean other = Boolean.parseBoolean(profile.getData("state." + rules.leftId));
				
				if(!other) {
					return true;
				}
				
				return event.getResult().isAllowed();
			}
			
			return true;
		}
	}
}
