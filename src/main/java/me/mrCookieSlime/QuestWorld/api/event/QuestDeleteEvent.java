package me.mrCookieSlime.QuestWorld.api.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.IQuest;

public class QuestDeleteEvent extends CancellableEvent {
	private IQuest quest;
	
	public QuestDeleteEvent(IQuest quest) {
		this.quest = quest;
	}
	
	public IQuest getQuest() {
		return quest;
	}

	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
