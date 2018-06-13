package com.questworld.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.questworld.api.contract.IMission;

public class MissionCompletedEvent extends Event {
	private IMission mission;

	public MissionCompletedEvent(IMission mission) {
		this.mission = mission;
	}

	public IMission getMission() {
		return mission;
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
