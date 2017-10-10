package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;

public class MissionDeleteEvent extends CancellableEvent {
	private IMission mission;
	
	public MissionDeleteEvent(IMission mission) {
		this.mission = mission;
	}
	
	public IMission getMission() {
		return mission;
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
