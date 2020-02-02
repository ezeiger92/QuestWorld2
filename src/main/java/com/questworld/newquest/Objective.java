package com.questworld.newquest;

import java.util.ArrayList;
import java.util.List;

import com.questworld.util.UniqueKey;

public class Objective {
	private UniqueKey task;
	private List<Condition> rules = new ArrayList<>();
	
	public Objective(UniqueKey task) {
		this.task = task;
	}
	
	
}
