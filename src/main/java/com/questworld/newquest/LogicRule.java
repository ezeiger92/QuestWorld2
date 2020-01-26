package com.questworld.newquest;

import org.bukkit.event.Event;

// Wrong, inner rules are dynamic so should be in an instance
// Why did I put this in the "concrete" type?
// Oh right because I was tired
@Deprecated()
public class LogicRule {
	private LogicRule() {	
	}
	
	public static class And extends Rule {
		private final Rule[] rules;
		
		public And(Rule... rules) {
			super(MakeRuleKey("and"));
			this.rules = rules;
		}
	}
	
	public static class Or extends Rule {
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
	}
}
