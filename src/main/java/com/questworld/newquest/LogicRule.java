package com.questworld.newquest;

import java.util.List;

import org.bukkit.event.Event;

import com.questworld.newquest.event.RuleResultEvent;

public class LogicRule {
	private LogicRule() {	
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
	
	// Not a good instance holder
	public static class RuleList {
		public List<Integer> instanceIds;
	}
	
	
	
	/*public static class Or extends Rule {
		private final Rule[] rules;
		
		public Or(Rule... rules) {
			super(MakeRuleKey("or"));
			this.rules = rules;
		}
	}
	
	public static class Not extends Rule {
		private final Rule rule;
		
		public Not(Rule rule) {
			super(MakeRuleKey("not"));
			this.rule = rule;
		}
	}
	
	public static class Xor extends Rule {
		private final Rule rule1;
		private final Rule rule2;
		
		public Xor(Rule rule1, Rule rule2) {
			super(MakeRuleKey("xor"));
			this.rule1 = rule1;
			this.rule2 = rule2;
		}
	}
	
	public static class Iff extends Rule {
		private final Rule rule1;
		private final Rule rule2;
		
		public Iff(Rule rule1, Rule rule2) {
			super(MakeRuleKey("iff"));
			this.rule1 = rule1;
			this.rule2 = rule2;
		}
	}
	
	public static class Implies extends Rule {
		private final Rule rule1;
		private final Rule rule2;
		
		public Implies(Rule rule1, Rule rule2) {
			super(MakeRuleKey("implies"));
			this.rule1 = rule1;
			this.rule2 = rule2;
		}
	}*/
}
