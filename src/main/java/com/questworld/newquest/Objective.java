package com.questworld.newquest;

import java.util.ArrayList;
import java.util.List;

import com.questworld.util.UniqueKey;

public class Objective {
	private UniqueKey task;
	
	// Condition IDs. Still a bad holder
	private List<Integer> rules = new ArrayList<>();
	private List<Integer> goals = new ArrayList<>();
	
	public Objective(UniqueKey task) {
		this.task = task;
	}
	
	
}
