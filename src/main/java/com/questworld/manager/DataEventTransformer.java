package com.questworld.manager;

import java.util.function.Consumer;
import java.util.function.Function;

import com.questworld.api.contract.DataObject;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface DataEventTransformer extends Function<DataObject, Consumer<InventoryClickEvent>> {

	default void execute(DataObject object, InventoryClickEvent event) {
		Consumer<InventoryClickEvent> listener = apply(object);

		if(listener != null) {
			listener.accept(event);
		}
	}
}
