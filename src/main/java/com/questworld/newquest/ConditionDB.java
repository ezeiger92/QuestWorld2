package com.questworld.newquest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionDB {
	private final Map<Class<? extends Rule>, List<Condition>> data;
	
	public ConditionDB() {
		data = new HashMap<>();
	}
	
	public Collection<Condition> getConditions(Class<? extends Rule> forRule) {
		return Collections.unmodifiableList(data.getOrDefault(forRule, Collections.emptyList()));
	}
	
	public void registerConditions(Class<? extends Rule> forRule, Condition... conditions) {
		List<Condition> existing = data.get(forRule);
		
		if(existing == null) {
			data.put(forRule, existing = new ArrayList<>());
		}

		existing.addAll(Arrays.asList(conditions));
	}
	
	public void clearConditions(Class<? extends Rule> forRule) {
		data.remove(forRule);
	}
}
