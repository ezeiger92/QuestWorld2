package me.mrCookieSlime.QuestWorld.api.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryState;

public class CategoryChangeEvent extends CancellableEvent {
	private ICategoryState nextState;
	
	public CategoryChangeEvent(ICategoryState nextState) {
		this.nextState = nextState;
	}
	
	public ICategory getCategory() {
		return nextState.getSource();
	}

	public ICategoryState getNextState() {
		return nextState;
	}
	
	public boolean hasChange(ICategoryState.Member field) {
		return nextState.hasChange(field);
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
