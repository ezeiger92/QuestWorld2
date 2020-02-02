package com.questworld.newquest.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.questworld.newquest.Condition;

public class ConditionCheckEvent extends Event {
	
	public Condition getRule() {
		return null;
	}
	
	public boolean getResult() {
		return false;
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
