package com.questworld.newquest.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RuleResultEvent extends Event {

	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();
}
