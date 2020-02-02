package com.questworld.newquest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigDB<ClassType, ConfigType> {
	private final Map<Class<ClassType>, List<NodeConfig<ConfigType>>> data;
	
	public ConfigDB() {
		data = new HashMap<>();
	}
	
	public Collection<NodeConfig<ConfigType>> getConfigs(Class<? extends ClassType> forRule) {
		return Collections.unmodifiableList(data.getOrDefault(forRule, Collections.emptyList()));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void storeConfigs(Class<? extends ClassType> forRule, NodeConfig<? extends ConfigType>... configs) {
		List<NodeConfig<ConfigType>> existing = data.get(forRule);
		
		if(existing == null) {
			data.put((Class)forRule, existing = new ArrayList<>());
		}

		existing.addAll((List)Arrays.asList(configs));
	}
	
	public void clearConfigs(Class<? extends ClassType> forRule) {
		data.remove(forRule);
	}
}
