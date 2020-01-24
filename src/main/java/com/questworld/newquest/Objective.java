package com.questworld.newquest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;

public class Objective {
	private NamespacedKey task;
	private List<Rule> rules = new ArrayList<>();
	
	public Objective(NamespacedKey task) {
		this.task = task;
	}
	
	
}
