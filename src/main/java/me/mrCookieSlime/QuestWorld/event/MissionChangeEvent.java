package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.quest.Mission;

public class MissionChangeEvent extends CancellableEvent {
	private MissionChange nextState;
	
	public MissionChangeEvent(MissionChange nextState) {
		this.nextState = nextState;
	}
	
	public Mission getMission() {
		return nextState.getSource();
	}

	public MissionChange getNextState() {
		return nextState;
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
