package com.questworld.newquest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleConfigDB {
	private final Map<Class<? extends Rule>, List<NodeConfig>> data;
	
	public RuleConfigDB() {
		data = new HashMap<>();
	}
	
	public Collection<NodeConfig> getConfigs(Class<? extends Rule> forRule) {
		return Collections.unmodifiableList(data.getOrDefault(forRule, Collections.emptyList()));
	}
	
	public void storeConfigs(Class<? extends Rule> forRule, NodeConfig... configs) {
		List<NodeConfig> existing = data.get(forRule);
		
		if(existing == null) {
			data.put(forRule, existing = new ArrayList<>());
		}

		existing.addAll(Arrays.asList(configs));
	}
	
	public void clearConfigs(Class<? extends Rule> forRule) {
		data.remove(forRule);
	}
}
