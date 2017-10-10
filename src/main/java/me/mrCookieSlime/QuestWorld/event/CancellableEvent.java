package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * Simple event base for events that can be cancelled
 */
public abstract class CancellableEvent extends Event implements Cancellable {
	private boolean cancelled;
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
	
	public static boolean send(CancellableEvent event) {
		Bukkit.getPluginManager().callEvent(event);
		
		return !event.isCancelled();
	}
	
/*
	COPY PASTE ME INTO DERIVED EVENTS :D

	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
*/
}
