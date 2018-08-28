package com.questworld.manager;

import java.util.HashMap;
import java.util.Locale;
import java.util.function.Consumer;

import com.questworld.api.contract.DataObject;

import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuRegistry {
	private HashMap<String, DataEventTransformer> functions = new HashMap<>();

	public void register(String key, DataEventTransformer value) {
		functions.put(key.toLowerCase(Locale.ENGLISH), value);
	}

	public DataEventTransformer get(String key) {
		return functions.get(key.toLowerCase(Locale.ENGLISH));
	}

	public void execute(String key, DataObject object, InventoryClickEvent event) {
		DataEventTransformer function = get(key);

		if(function != null) {
			function.execute(object, event);
		}
	}

	public Consumer<InventoryClickEvent> getConsumer(String key, DataObject object) {
		DataEventTransformer transformer = get(key);

		if(key != null) {
			return transformer.apply(object);
		}

		return null;
	}
}
