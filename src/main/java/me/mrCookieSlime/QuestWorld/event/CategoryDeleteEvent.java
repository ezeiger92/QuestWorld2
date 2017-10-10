package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.ICategory;

public class CategoryDeleteEvent extends CancellableEvent {
	private ICategory category;
	
	public CategoryDeleteEvent(ICategory category) {
		this.category = category;
	}
	
	public ICategory getCategory() {
		return category;
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
