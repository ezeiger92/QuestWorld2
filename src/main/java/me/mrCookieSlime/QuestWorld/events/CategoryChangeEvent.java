package me.mrCookieSlime.QuestWorld.events;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.CategoryChange;
import me.mrCookieSlime.QuestWorld.quests.Category;

public class CategoryChangeEvent extends CancellableEvent {
	private CategoryChange nextState;
	
	public CategoryChangeEvent(CategoryChange nextState) {
		this.nextState = nextState;
	}
	
	public Category getCategory() {
		return nextState.getSource();
	}

	public CategoryChange getNextState() {
		return nextState;
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
