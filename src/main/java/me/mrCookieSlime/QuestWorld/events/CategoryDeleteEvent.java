package me.mrCookieSlime.QuestWorld.events;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.quests.Category;

public class CategoryDeleteEvent extends CancellableEvent {
	private Category category;
	
	public CategoryDeleteEvent(Category category) {
		this.category = category;
	}
	
	public Category getCategory() {
		return category;
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
