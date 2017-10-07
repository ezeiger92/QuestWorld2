package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.QuestChange;
import me.mrCookieSlime.QuestWorld.quest.Quest;

/**
 * An event fired before applying a set of changes to a quest
 */
public class QuestChangeEvent extends CancellableEvent {
	private QuestChange nextState;

	public QuestChangeEvent(QuestChange nextState) {
		this.nextState = nextState;
	}

	public Quest getQuest() {
		return nextState.getSource();
	}

	public QuestChange getNextState() {
		return nextState;
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
