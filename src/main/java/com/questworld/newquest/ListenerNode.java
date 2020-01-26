package com.questworld.newquest;

import java.util.Map;

import org.bukkit.event.Listener;

public abstract class ListenerNode<T> implements Listener {
	private final ListenerNode<?> parent;
	private final Map<String, Object> properties;
	private final T object;
	
	public ListenerNode(ListenerNode<?> parent, Map<String, Object> properties, T object) {
		this.parent = parent;
		this.properties = properties;
		this.object = object;
	}

	public ListenerNode<?> getParent() {
		return parent;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public T getObject() {
		return object;
	}
}
