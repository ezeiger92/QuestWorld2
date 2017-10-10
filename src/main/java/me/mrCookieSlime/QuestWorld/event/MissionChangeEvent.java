package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.quest.MissionChange;

public class MissionChangeEvent extends CancellableEvent {
	private MissionChange nextState;
	
	public MissionChangeEvent(MissionChange nextState) {
		this.nextState = nextState;
	}
	
	public IMission getMission() {
		return nextState.getSource();
	}

	public IMissionWrite getNextState() {
		return nextState;
	}
	
	public boolean hasChange(IMissionWrite.Member field) {
		return nextState.hasChange(field);
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
