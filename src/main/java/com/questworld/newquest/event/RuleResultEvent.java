package com.questworld.newquest.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.questworld.newquest.Rule;

public class RuleResultEvent extends Event {
	
	public Rule getRule() {
		return null;
	}
	
	public Rule.Result getResult() {
		return null;
	}
	
	public int getInstanceId() {
		return 0;
	}

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
