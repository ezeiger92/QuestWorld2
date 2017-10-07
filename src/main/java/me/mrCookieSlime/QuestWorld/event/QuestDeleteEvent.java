package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.quest.Quest;

public class QuestDeleteEvent extends CancellableEvent {
	private Quest quest;
	
	public QuestDeleteEvent(Quest quest) {
		this.quest = quest;
	}
	
	public Quest getQuest() {
		return quest;
	}

	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
